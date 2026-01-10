import com.android.build.gradle.BaseExtension
import helpers.applyPlugins
import helpers.catalog
import helpers.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("kotlin-compose-compiler", catalog = catalog)

            dependencies {
                "implementation"(library("androidx.compose.material3"))
                "implementation"(library("androidx.compose.ui"))
                "implementation"(library("androidx.compose.ui.tooling.preview"))
                "debugImplementation"(library("androidx.compose.ui.tooling"))
                "implementation"(library("androidx.compose.activity"))
                "implementation"(library("androidx.compose.animation"))
                "implementation"(library("androidx.compose.foundation"))
                "implementation"(library("androidx.compose.material.icons"))
                "implementation"(library("androidx.lifecycle.compose"))
                "lintChecks"(library("compose.lint.checks"))
                "implementation"(library("w2sv.composed.core"))
                "implementation"(library("w2sv.composed.material3"))
            }

            extensions.apply {
                configure<BaseExtension> {
                    buildFeatures.compose = true
                    configure<KotlinAndroidProjectExtension> {
                        compilerOptions.freeCompilerArgs.addAll(
                            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
                            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
                        )
                    }
                }
            }
        }
    }
}
