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
//    defaultConfig {
//        testInstrumentationRunner = "com.w2sv.navigator.HiltTestRunner"
//    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.viewmodel)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.materialDialogs.core)

    implementation(libs.google.guava)

    implementation(libs.simplestorage)

    // ==============
    // Test
    // ==============

    testImplementation(projects.core.test)

    // ==============
    // Android Test
    // ==============

//    androidTestImplementation(libs.androidx.test.runner)
//    androidTestImplementation(libs.androidx.test.rules)
//    androidTestImplementation(libs.androidx.test.uiautomator)
//    androidTestImplementation(libs.androidx.test.ext.junit)
//
//    androidTestImplementation(libs.google.hilt.android.testing)
//    androidTestImplementation(projects.core.database)
//    androidTestImplementation(projects.core.datastore)
//    kspAndroidTest(libs.google.hilt.compiler)
}
