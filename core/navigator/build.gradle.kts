plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.viewmodel)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.google.guava)

    implementation(libs.simplestorage)

    // ---------------
    // Test

    testImplementation(projects.core.test)
}
