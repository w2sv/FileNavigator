import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused")
class LibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            val libs: VersionCatalog = getVersionCatalog()

            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_17.toString()
                }
            }
            extensions.configure<KotlinProjectExtension> {
                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(libs.findConvertedVersion("java")))
                }
            }
            extensions.configure<BaseExtension> {
                namespace = "com.w2sv." + path.removePrefix(":").replace(':', '.')
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                defaultConfig {
                    multiDexEnabled = true
                    minSdk = libs.findConvertedVersion("minSdk")
                    targetSdk = libs.findConvertedVersion("compileSdk")
                }
                compileSdkVersion(libs.findConvertedVersion("compileSdk"))
                testOptions {
                    unitTests.isReturnDefaultValues = true
                    animationsDisabled = true
                    unitTests.isIncludeAndroidResources = true
                }
            }

//            dependencies {
//                add("testImplementation", kotlin("test"))
//                add("testImplementation", project(":core:testing"))
//                add("androidTestImplementation", kotlin("test"))
//                add("androidTestImplementation", project(":core:testing"))
//            }
        }
    }
}

private fun VersionCatalog.findConvertedVersion(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

private fun Project.getVersionCatalog(catalogName: String = "libs"): VersionCatalog =
    extensions.getByType(VersionCatalogsExtension::class.java).named(catalogName)