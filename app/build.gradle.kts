import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.play)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    val packageName = "com.w2sv.filenavigator"

    namespace = packageName
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()

        versionCode = project.findProperty("versionCode")!!.toString().toInt()
        versionName = version.toString()

        // Store bundles as "{versionName}-{buildFlavor}.aab"
        archivesName = versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            rootProject.file("keystore.properties").let { file ->
                if (file.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(file))

                    storeFile = rootProject.file("keys.jks")
                    storePassword = keystoreProperties["storePassword"] as String
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                }
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    hilt {
        enableAggregatingTask = true
    }

    // Name built apks "{versionName}.apk"
    applicationVariants.all {
        outputs
            .forEach { output ->
                (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                    "${versionName}.apk"
            }
    }

    kotlinOptions {
        freeCompilerArgs += listOf(
            // Apply compose_compiler_config.conf
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/compose_compiler_config.conf",
            // Enable strong skipping
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:experimentalStrongSkipping=true"
        )
    }
}

// https://github.com/Triple-T/gradle-play-publisher
play {
    serviceAccountCredentials.set(file("service-account-key.json"))
    defaultToAppBundles.set(true)
    artifactDir.set(file("build/outputs/bundle/release"))
}

dependencies {
    implementation(projects.core.datastorage)
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.navigator)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.composed)
    implementation(libs.composed.permissions)

    // Androidx
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    // .Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.compose.destinations)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.androidx.hilt.navigation.compose)

    // Other
    implementation(libs.slimber)
    implementation(libs.google.guava)
    implementation(libs.simplestorage)
    implementation(libs.kotlinx.collections.immutable)
    lintChecks(libs.compose.lint.checks)
    implementation(libs.materialKolor)

    // Unit Test
    testImplementation(libs.bundles.unitTest)

    // Android Test
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.rules)
}
