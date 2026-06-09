plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.resources)
    api(projects.modules.storage)
    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.simplestorage)

    // ==============
    // Test
    // ==============

    testImplementation(projects.modules.test)
}
