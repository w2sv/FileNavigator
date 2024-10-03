plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.filenavigator.room)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(libs.androidx.core)
    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.simplestorage)
}

dependencies {
    testImplementation(libs.bundles.unitTest)
}

dependencies {
    androidTestImplementation(libs.bundles.instrumentationTest)
    androidTestImplementation(libs.androidx.room.testing)
}