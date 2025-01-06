package helpers

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * @param alias The version alias that will be passed to [VersionCatalog.findVersion]
 */
internal fun VersionCatalog.findVersionInt(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

/**
 * @param alias The plugin alias that will be passed to [VersionCatalog.findPlugin]
 */
internal fun VersionCatalog.findPluginId(alias: String): String =
    findPlugin(alias).get().get().pluginId

internal val Project.catalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * @param alias The library alias that will be passed to [VersionCatalog.findLibrary]
 */
internal fun Project.library(alias: String): Provider<MinimalExternalModuleDependency> =
    catalog.findLibrary(alias).get()

internal fun PluginManager.applyPlugins(vararg pluginId: String, catalog: VersionCatalog) {
    pluginId.forEach {
        apply(catalog.findPluginId(it))
    }
}
