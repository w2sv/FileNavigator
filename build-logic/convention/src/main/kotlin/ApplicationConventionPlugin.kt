import helpers.Namespace
import helpers.applyBaseConfig
import helpers.applyPlugins
import helpers.catalog
import org.gradle.api.Plugin
import org.gradle.api.Project

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("android-application", "kotlin-android", catalog = catalog)
            applyBaseConfig(Namespace.Manual("com.w2sv.wifiwidget"))
        }
    }
}
