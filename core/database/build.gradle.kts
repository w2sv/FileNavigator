plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.filenavigator.room)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.core)

    implementation(libs.androidutils)
    implementation(libs.slimber)

    implementation(libs.simplestorage)
}