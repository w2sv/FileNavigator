plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

dependencies {
    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.simplestorage)

    implementation(libs.androidx.core)

    // Hilt
    implementation(libs.google.hilt)
    ksp(libs.google.hilt.compiler)

    // Unit tests
    testImplementation(libs.bundles.unitTest)
}
