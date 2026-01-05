import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.play)
    alias(libs.plugins.filenavigator.application)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

android {
    defaultConfig {
        applicationId = namespace
        versionCode = project.property("versionCode").toString().toInt()
        versionName = version.toString()
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            buildStartScreenConfigField(retrieveStartScreenFromLocalProperties())
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = releaseSigningConfigOrNull()
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildStartScreenConfigField("")
            // isDebuggable = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        checkDependencies = true
        xmlReport = false
        htmlReport = true
        textReport = false
        htmlOutput = project.layout.buildDirectory.file("reports/lint-results-debug.html").get().asFile
    }
    // Name built apks "{versionName}.apk"
    applicationVariants.all {
        outputs
            .forEach { output ->
                (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                    "$versionName.apk"
            }
    }
    dependenciesInfo {
        // Disable dependency metadata when building APKs for fdroid reproducibility
        includeInApk = false
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
            )
        }
    }
}

private fun BaseAppModuleExtension.releaseSigningConfigOrNull(): ApkSigningConfig? {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties().apply { load(FileInputStream(keystorePropertiesFile)) }
        return signingConfigs.create("release") {
            storeFile = rootProject.file("keys.jks")
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    logger.warn("Couldn't create signing config; ${keystorePropertiesFile.path} does not exist")
    return null
}

private fun retrieveStartScreenFromLocalProperties(default: String = ""): String {
    val fileName = "local.properties"
    val propertyName = "startScreen"

    return try {
        val props = Properties().apply {
            load(FileInputStream(rootProject.file(fileName)))
        }
        props.getProperty(propertyName) ?: error("Couldn't find property '$propertyName' in $fileName")
    } catch (e: Exception) {
        logger.warn(e.message)
        default
    }
}

private fun VariantDimension.buildStartScreenConfigField(value: String) {
    buildConfigField(
        "String",
        "START_SCREEN",
        "\"$value\""
    )
}

androidComponents {
    onVariants(selector().withBuildType("release")) {
        it.packaging.resources.excludes.add("META-INF/**")
    }
}

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html#compose-compiler-options-dsl
composeCompiler {
    includeSourceInformation = true
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_compiler_config.conf"))
    metricsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
    reportsDestination.set(project.layout.buildDirectory.dir("compose_compiler"))
}

// https://github.com/Triple-T/gradle-play-publisher
play {
    serviceAccountCredentials.set(file("service-account-key.json"))
    defaultToAppBundles.set(true)
    artifactDir.set(file("build/outputs/bundle/release"))
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.navigator)
    implementation(projects.core.datastore)
    implementation(projects.core.database)
    baselineProfile(projects.benchmarking)

    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.reversiblestate)
    implementation(libs.w2sv.composed.core)
    implementation(libs.w2sv.composed.material3)
    implementation(libs.w2sv.composed.permissions)
    implementation(libs.w2sv.colorpicker)

    // Androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)

    // .Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)

    // Kotlinx
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.core)

    // Other
    implementation(libs.slimber)
    implementation(libs.google.guava)
    implementation(libs.w2sv.simplestorage)
    lintChecks(libs.compose.lint.checks)
    implementation(libs.textflow)
    // Workaround for https://github.com/google/dagger/issues/5059
    ksp(libs.kotlin.metadata.jvm)

    // Unit Test
    testImplementation(libs.bundles.unitTest)

    // Android Test
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.rules)
}
