import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import fyi.kittens.ozone.api.generator.ApiReturnType

plugins {
  id("ozone-dokka")
  id("ozone-multiplatform")
  id("ozone-publish")
  id("co.touchlab.kmmbridge")
  id("fyi.kittens.ozone.generator")
}

ozone {
  js()
  jvm()
  ios("BlueskyAPI") {
    project.configurations[exportConfigurationName].extendsFrom(
      project.configurations["${target.name}CompilationApi"]
    )

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    transitiveExport = true
  }
}

kmmbridge {
  mavenPublishArtifacts()
  spm()
}

dependencies {
  lexicons(project(":lexicons"))
}

lexicons {
  namespace.set("fyi.kittens.ozone.api.xrpc")

  defaults {
    generateUnknownsForSealedTypes.set(true)
    generateUnknownsForEnums.set(true)
  }

  generateApi("BlueskyApi") {
    packageName.set("fyi.kittens.ozone")
    withKtorImplementation("XrpcBlueskyApi")
    returnType.set(ApiReturnType.Response)
  }
}

val generateLexicons = tasks.generateLexicons
tasks.apiDump.configure { dependsOn(generateLexicons) }
tasks.apiCheck.configure { dependsOn(generateLexicons) }

tasks.withType<DokkaBaseTask>().configureEach {
  dependsOn(tasks.withType<KotlinCompile>())
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":oauth"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.test)
        implementation(kotlin("test"))
      }
    }
  }
}
