package helpers

import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.applyBaseConfig(excludeMetaInfResources: Boolean = true, namespace: Namespace = Namespace.Auto) {
    pluginManager.applyPlugins("ktlint", catalog = catalog)

    dependencies {
        "implementation"(bundle("w2sv-kotlinutils"))
        "implementation"(library("slimber"))
    }

    extensions.configureKotlinAndroidProject()
    extensions.configureCommon(
        namespace = namespace.get(path),
        catalog = catalog,
        excludeMetaInfResources = excludeMetaInfResources
    )
    enableTestParallelization()
    setRobolectricSdk(this)
}

private fun ExtensionContainer.configureKotlinAndroidProject() {
    configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi",
                "-Xannotation-default-target=param-property",
                "-Xwhen-guards",
                "-XXLanguage:+ExplicitBackingFields",
                "-Xnested-type-aliases",
                "-Xcontext-sensitive-resolution",
                "-Xcontext-parameters"
            )
        }
    }
}

private fun ExtensionContainer.configureCommon(
    namespace: String,
    catalog: VersionCatalog,
    excludeMetaInfResources: Boolean
) {
    configure<CommonExtension> {
        this.namespace = namespace
        defaultConfig.apply {
            minSdk = catalog.findVersionInt("minSdk")
            compileSdk = catalog.findVersionInt("compileSdk")
            (this as? ApplicationDefaultConfig)?.run { targetSdk = catalog.findVersionInt("compileSdk") }
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        testOptions.apply {
            unitTests {
                isReturnDefaultValues = true
                isIncludeAndroidResources = true
                all { test -> test.failOnNoDiscoveredTests.set(false) }
            }
            animationsDisabled = true
        }
        if (excludeMetaInfResources) {
            packaging.resources.excludes.add("/META-INF/*")
        }
    }
}

/**
 * https://docs.gradle.org/current/userguide/performance.html#a_run_tests_in_parallel
 */
private fun Project.enableTestParallelization() {
    tasks.withType(Test::class.java).configureEach {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }
}
