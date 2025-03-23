plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.simplestorage)
    implementation(libs.slimber)

    implementation(libs.androidx.core)

    // Unit tests
    testImplementation(projects.core.test)
}
