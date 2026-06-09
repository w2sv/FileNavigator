plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
}

dependencies {
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core.ktx)

    testImplementation(projects.modules.test)
}
