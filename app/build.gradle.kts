import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.play)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
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

//    signingConfigs {
//        create("release") {
//            rootProject.file("keystore.properties").let { file ->
//                if (file.exists()) {
//                    val keystoreProperties = Properties()
//                    keystoreProperties.load(FileInputStream(file))
//
//                    storeFile = rootProject.file("keys.jks")
//                    storePassword = keystoreProperties["storePassword"] as String
//                    keyAlias = keystoreProperties["keyAlias"] as String
//                    keyPassword = keystoreProperties["keyPassword"] as String
//                } else {
//                    println("WARNING: Failed to create release signing configuration; ${file.path} not present")
//                }
//            }
//        }
//    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig = signingConfigs.getByName("release")
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
        jvmTarget = libs.versions.java.get().toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    hilt {
        enableAggregatingTask = true
    }

//    applicationVariants.configureEach { variant ->
//        variant.outputs.configureEach {
//            outputFileName = "${variant.versionName}.apk"
//        }
//    }
}

// https://github.com/Triple-T/gradle-play-publisher
//configure<PlayExtension> {
//    serviceAccountCredentials.set(file("../service-account-key.json"))
//    defaultToAppBundles.set(true)
//    artifactDir.set(file("build/outputs/bundle/release"))
//}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":navigator"))

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)

    // Androidx
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    // .Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.viewmodel)
    implementation(libs.androidx.lifecycle.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.google.hilt)
    ksp(libs.google.hilt.compiler)

    // Other
    implementation(libs.slimber)
    implementation(libs.accompanist.permissions)
    implementation(libs.google.guava)
    implementation(libs.simplestorage)

    // Unit Test
    testImplementation(libs.bundles.unitTest)

    // Android Test
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.rules)
}
