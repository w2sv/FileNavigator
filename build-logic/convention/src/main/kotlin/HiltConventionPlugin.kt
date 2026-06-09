import helpers.applyPlugins
import helpers.catalog
import helpers.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("ksp", "hilt", catalog = catalog)

            dependencies {
                "implementation"(library("google.hilt"))
                "ksp"(library("google.hilt.compiler"))
                // Fix [Hilt] Provided Metadata instance has version 2.4.0, while maximum supported version is 2.3.0. To support newer versions, update the kotlin-metadata-jvm library.
                "ksp"(library("kotlin.metadata.jvm"))
            }
        }
    }
}
