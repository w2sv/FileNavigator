plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.filenavigator.room)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.common)
    implementation(projects.domain)

    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidutils)
    implementation(libs.slimber)

    implementation(libs.simplestorage)

    // ---------------
    // Test

    testImplementation(projects.test)
}