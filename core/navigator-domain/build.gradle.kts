plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)

    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.w2sv.simplestorage)

    // ==============
    // Test
    // ==============

    testImplementation(projects.core.test)
}
