import org.gradle.api.Plugin
import org.gradle.api.Project

class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPluginId("android.library"))
                apply(libs.findPluginId("kotlin.android"))
            }
            baseConfig()
        }
    }
}