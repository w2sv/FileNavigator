plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.simplestorage)

    implementation(libs.androidx.core)

    // Unit tests
    testImplementation(projects.core.test)
}
