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
                    languageVersion.set(JavaLanguageVersion.of(libs.findVersionInt("java")))
                }
            }
            extensions.configure<BaseExtension> {
                namespace = "com.w2sv." + path.removePrefix(":").replace(':', '.')
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                defaultConfig {
                    minSdk = libs.findVersionInt("minSdk")
                    targetSdk = libs.findVersionInt("compileSdk")
                }
                compileSdkVersion(libs.findVersionInt("compileSdk"))
                testOptions {
                    unitTests.isReturnDefaultValues = true
                    animationsDisabled = true
                    unitTests.isIncludeAndroidResources = true
                }
            }
        }
    }
}