import com.android.build.gradle.BaseExtension
import helpers.Namespace
import helpers.applyBaseConfig
import helpers.applyPlugins
import helpers.catalog
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("android-application", "kotlin-android", catalog = catalog)
            applyBaseConfig(excludeMetaInfResources = false, namespace = Namespace.Manual("com.w2sv.filenavigator"))
        }
    }
}
