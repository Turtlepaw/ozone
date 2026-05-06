package sh.christian.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class BaseAndroidPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.applyPlugin()
}

private fun Project.applyPlugin() {
  plugins.apply("ozone-base")

  pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension>("android") {
      compileSdk = 36

      defaultConfig {
        minSdk = 30
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
  }

  pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension>("android") {
      compileSdk = 36

      defaultConfig {
        minSdk = 30
      }

      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
  }
}
