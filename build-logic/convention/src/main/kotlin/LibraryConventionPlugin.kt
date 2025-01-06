import helpers.applyBaseConfig
import helpers.applyPlugins
import helpers.catalog
import org.gradle.api.Plugin
import org.gradle.api.Project

class LibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("android-library", "kotlin-android", catalog = catalog)
            applyBaseConfig()
        }
    }
}
