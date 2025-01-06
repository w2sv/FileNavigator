package helpers

import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal sealed interface Namespace {
    object Auto: Namespace

    @JvmInline
    value class Manual(val namespace: String): Namespace
}

internal fun Project.applyBaseConfig(namespace: Namespace = Namespace.Auto) {
    pluginManager.applyPlugins("ktlint", catalog = catalog)

    extensions.apply {
        configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
        configure<BaseExtension> {
            this.namespace = when (namespace) {
                is Namespace.Auto -> "com.w2sv." + path.removePrefix(":").replace(':', '.')  // Sets namespace to "com.w2sv.<module-name>"
                is Namespace.Manual -> namespace.namespace
            }

            defaultConfig {
                minSdk = catalog.findVersionInt("minSdk")
                targetSdk = catalog.findVersionInt("compileSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            compileSdkVersion(catalog.findVersionInt("compileSdk"))
            testOptions {
                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true
                }
                animationsDisabled = true
            }
            packagingOptions {
                resources {
                    excludes.add("/META-INF/*")
                }
            }
        }
    }
}
