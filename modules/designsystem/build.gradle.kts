plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.compose)
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.resources)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.androidx.constraintlayout.compose)

    testImplementation(projects.modules.shared)
    testImplementation(libs.bundles.unitTest)
}
