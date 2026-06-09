plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.compose)
}

dependencies {
    implementation(projects.modules.common)
    implementation(projects.modules.domain)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.androidx.constraintlayout.compose)

    testImplementation(libs.bundles.unitTest)
}
