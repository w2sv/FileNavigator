plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    id("kotlin-parcelize")
}

dependencies {
    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.simplestorage)

    implementation(libs.androidx.core)

    // Unit tests
    testImplementation(libs.bundles.unitTest)
}
