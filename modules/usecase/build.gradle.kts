plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
}

dependencies {
    implementation(projects.modules.domain)

    implementation(libs.w2sv.persistedPreferences)
    implementation(libs.w2sv.simplestorage)
    implementation(libs.androidx.core.ktx)
}
