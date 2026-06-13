plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.filenavigator.room)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.directories.add("$projectDir/schemas")
    }
}

dependencies {
    implementation(projects.modules.shared)
    implementation(projects.modules.domain)
    implementation(projects.modules.storage)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.simplestorage)

    testImplementation(libs.bundles.unitTest)

    androidTestImplementation(libs.bundles.instrumentationTest)
    androidTestImplementation(libs.androidx.room.testing)
}
