package helpers

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * There is no global way to set the Robolectric SDK version, so each module uses the shared
 * robolectric.properties as a test resource.
 */
fun setRobolectricSdk(target: Project) {
    target.extensions.configure<CommonExtension> {
        sourceSets.named("test") {
            resources.directories.add(target.rootProject.file("config/robolectric").path)
        }
    }
}
