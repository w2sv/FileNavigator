import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.baseConfig() {
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
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileSdkVersion(libs.findVersionInt("compileSdk"))
        testOptions {
            unitTests.isReturnDefaultValues = true
            animationsDisabled = true
            unitTests.isIncludeAndroidResources = true
        }
    }
}