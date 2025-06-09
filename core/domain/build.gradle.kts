plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)

    api(libs.w2sv.datastoreutils.datastoreflow)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.slimber)
    implementation(libs.w2sv.simplestorage)
    implementation(libs.androidx.core.ktx)

    testImplementation(projects.core.test)
}
