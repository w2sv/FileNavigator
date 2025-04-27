plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(libs.slimber)
    api(libs.bundles.unitTest)
}
