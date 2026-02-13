plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.simplestorage)
    implementation(libs.androidx.core.ktx)

    // Unit tests
    testImplementation(projects.modules.test)
}
