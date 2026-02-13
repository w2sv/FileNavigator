plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
}

dependencies {
    implementation(projects.modules.common)
    implementation(projects.modules.navigator)
    implementation(projects.modules.navigatorDomain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
}
