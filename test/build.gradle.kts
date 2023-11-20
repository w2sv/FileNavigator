plugins {
    alias(libs.plugins.filenavigator.library)
    id("kotlin-parcelize")
}

dependencies {
    api(libs.bundles.unitTest)
}