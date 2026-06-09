plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.modules.resources)
    api(projects.modules.storage)

    api(libs.w2sv.persistedPreferences)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.simplestorage)
    implementation(libs.androidx.core.ktx)

    testImplementation(projects.modules.test)
}
