import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPluginId("ksp"))
                apply(libs.findPluginId("hilt"))
            }

            dependencies {
                "implementation"(libs.findLibrary("google.hilt").get())
                "ksp"(libs.findLibrary("google.hilt.compiler").get())
            }
        }
    }
}