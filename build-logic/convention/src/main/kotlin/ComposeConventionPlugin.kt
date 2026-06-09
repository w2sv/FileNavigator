import com.android.build.api.dsl.CommonExtension
import helpers.applyPlugins
import helpers.catalog
import helpers.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("kotlin-compose-compiler", catalog = catalog)

            extensions.configure<CommonExtension> {
                buildFeatures.compose = true
            }

            // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html#compose-compiler-options-dsl
            extensions.configure<ComposeCompilerGradlePluginExtension> {
                stabilityConfigurationFiles.add(
                    rootProject.layout.projectDirectory.file("config/compose_compiler_config.conf")
                )
                includeSourceInformation.set(true)
                metricsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
                reportsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
            }

            tasks.withType<KotlinCompile> {
                compilerOptions {
                    optIn.addAll(
                        "androidx.compose.material3.ExperimentalMaterial3Api",
                        "androidx.compose.foundation.ExperimentalFoundationApi",
                        "androidx.compose.foundation.layout.ExperimentalLayoutApi",
                        "androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi"
                    )
                }
            }

            dependencies {
                "implementation"(library("androidx.compose.material3"))
                "implementation"(library("androidx.compose.ui"))
                "implementation"(library("androidx.compose.ui.tooling.preview"))
                "debugImplementation"(library("androidx.compose.ui.tooling"))
                "implementation"(library("androidx.compose.activity"))
                "implementation"(library("androidx.compose.animation"))
                "implementation"(library("androidx.compose.animation.graphics"))
                "implementation"(library("androidx.compose.foundation"))
                "implementation"(library("androidx.compose.material.icons"))
                "implementation"(library("androidx.compose.material.icons.extended"))
                "implementation"(library("androidx.lifecycle.compose"))
                "lintChecks"(library("compose.lint.checks"))
                "implementation"(library("w2sv.composed.core"))
                "implementation"(library("w2sv.composed.material3"))
            }
        }
    }
}
