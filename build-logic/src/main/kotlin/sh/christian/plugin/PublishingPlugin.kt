package sh.christian.plugin

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlinx.validation.ApiValidationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure

@Suppress("UnstableApiUsage", "unused")
class PublishingPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val groupId = target.stringProperty("POM_GROUP_ID")
    val artifactId = target.name
    val version = target.stringProperty("POM_VERSION")

    target.group = groupId
    target.version = version

    target.plugins.apply("org.jetbrains.kotlinx.binary-compatibility-validator")
    target.plugins.apply("com.vanniktech.maven.publish")

    target.extensions.configure<ApiValidationExtension> {
      nonPublicMarkers += "kotlin.Deprecated"
    }

    target.extensions.configure<MavenPublishBaseExtension> {
      coordinates(
        groupId = groupId,
        artifactId = artifactId,
        version = version,
      )

      pom {
        name.set(target.stringProperty("POM_NAME"))
        description.set(target.stringProperty("POM_DESCRIPTION"))
        inceptionYear.set("2023")
        url.set("https://github.com/christiandeange/ozone")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/license/mit/")
            distribution.set("repo")
          }
        }

        developers {
          developer {
            id.set("christiandeange")
            name.set("Christian De Angelis")
            url.set("https://github.com/christiandeange")
          }
        }

        scm {
          url.set("https://github.com/christiandeange/ozone")
          connection.set("scm:git:git://github.com/christiandeange/ozone.git")
          developerConnection.set("scm:git:ssh://git@github.com/christiandeange/ozone.git")
        }
      }

      publishToMavenCentral(automaticRelease = true)
      signAllPublications()
    }

    target.extensions.configure<PublishingExtension> {
      repositories {
        maven {
          name = "GitHubPackages"
          val githubRepository = target.findProperty("gpr.repo") as String?
            ?: System.getenv("GITHUB_REPOSITORY")
            ?: "Turtlepaw/ozone"
          url = target.uri("https://maven.pkg.github.com/$githubRepository")
          credentials {
            username = target.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = target.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
          }
        }
      }
    }
  }

  private fun Project.stringProperty(name: String): String {
    return property(name) as String
  }
}
