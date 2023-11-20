import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

fun VersionCatalog.findVersionInt(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

fun Project.getVersionCatalog(catalogName: String = "libs"): VersionCatalog =
    extensions.getByType(VersionCatalogsExtension::class.java).named(catalogName)