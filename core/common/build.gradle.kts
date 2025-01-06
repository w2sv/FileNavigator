plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.w2sv.androidutils)
    implementation(libs.slimber)
    implementation(libs.w2sv.simplestorage)

    implementation(libs.androidx.core)

    // Unit tests
    testImplementation(projects.core.test)
}
