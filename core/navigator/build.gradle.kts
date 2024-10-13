plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    defaultConfig {
        testInstrumentationRunner = "com.w2sv.navigator.HiltTestRunner"
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.google.guava)
    implementation(libs.simplestorage)
}

/**
 * Test dependencies.
 */
dependencies {
    testImplementation(projects.core.test)
}

/**
 * Android Test dependencies.
 */
dependencies {
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)

    androidTestImplementation(projects.core.database)
    androidTestImplementation(projects.core.datastore)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)

    // Hilt
    androidTestImplementation(libs.google.hilt.android.testing)
    kspAndroidTest(libs.google.hilt.compiler)
}