import android.databinding.tool.util.XmlEditor.attributes

plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(projects.flatteningparcelize)
    testImplementation(projects.core.test)
    // Use the specific variant needed by ksp
}
