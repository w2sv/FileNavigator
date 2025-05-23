plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.filenavigator.room)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.slimber)
    implementation(projects.storage)

    testImplementation(libs.bundles.unitTest)

    androidTestImplementation(libs.bundles.instrumentationTest)
    androidTestImplementation(libs.androidx.room.testing)
}
