plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.google.devtools.ksp.symbolProcessingApi)
    implementation(libs.squareup.kotlinpoet.ksp)

    testImplementation(projects.core.test)
}
