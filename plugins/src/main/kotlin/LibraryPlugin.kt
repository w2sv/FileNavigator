import org.gradle.api.Plugin
import org.gradle.api.Project

class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPluginId("library"))
                apply(libs.findPluginId("kotlin"))
            }

            baseConfig()
        }
    }
}