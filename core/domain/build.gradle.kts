plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.datastoreutils.datastoreflow)
    implementation(libs.androidutils)
    implementation(libs.slimber)
    implementation(libs.simplestorage)
    implementation(libs.androidx.core)

    testImplementation(projects.core.test)
}