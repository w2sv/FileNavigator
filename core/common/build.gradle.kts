plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(projects.storage)
    implementation(libs.slimber)

    implementation(libs.androidx.core.ktx)

    // Unit tests
    testImplementation(projects.core.test)
}
