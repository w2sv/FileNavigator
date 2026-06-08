import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

class StoreScreenshotsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val storeScreenshotsRequested = gradle.startParameter.taskNames.any {
                it.substringAfterLast(':') == STORE_SCREENSHOTS_TASK
            }
            val screenshotAndroidTestRequested = gradle.startParameter.taskNames.any {
                it.substringAfterLast(':').contains("ScreenshotAndroidTest")
            }
            val androidStudioModelSync = ANDROID_STUDIO_MODEL_PROPERTIES.any { property ->
                providers.gradleProperty(property).orNull?.toBoolean() == true
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    if (storeScreenshotsRequested) {
                        testInstrumentationRunnerArguments["class"] = SCREENSHOT_TEST_CLASS
                    }
                }

                buildTypes {
                    val debug = getByName("debug")
                    create(SCREENSHOT_BUILD_TYPE) {
                        initWith(debug)
                        applicationIdSuffix = ".screenshot"
                        matchingFallbacks += "debug"
                    }
                }

                // Model the screenshot sources during Studio sync, then preserve that variant when Studio runs it.
                testBuildType =
                    if (
                        storeScreenshotsRequested ||
                        screenshotAndroidTestRequested ||
                        androidStudioModelSync
                    ) {
                        SCREENSHOT_BUILD_TYPE
                    } else {
                        "debug"
                    }
                testOptions.managedDevices.localDevices.apply {
                    SCREENSHOT_DEVICES.forEach { (name, hardwareProfile) ->
                        create(name) {
                            device = hardwareProfile
                            apiLevel = 35
                            systemImageSource = "aosp"
                            testedAbi = "x86_64"
                        }
                    }
                }
            }

            val captureTasks = SCREENSHOT_DEVICES.keys.map {
                "${it}ScreenshotAndroidTest"
            }

            // AGP stages all managed-device outputs together, so parallel completion can overwrite screenshots.
            if (storeScreenshotsRequested) {
                tasks.configureEach {
                    captureTasks.zipWithNext().forEach { (previous, current) ->
                        if (name == current) {
                            mustRunAfter(previous)
                        }
                    }
                }
            }

            val generatedOutputs =
                layout.buildDirectory.dir("outputs/managed_device_android_test_additional_output/screenshot")
            val intermediateOutputs =
                layout.buildDirectory.dir(
                    "intermediates/managed_device_android_test_additional_output/screenshotAndroidTest"
                )
            val playStoreGraphics = layout.projectDirectory.dir("src/main/play/listings/en-US/graphics")

            val syncTasks = SCREENSHOT_DIRECTORIES.map { directoryName ->
                tasks.register<Sync>("sync${directoryName.toTaskName()}") {
                    dependsOn(captureTasks)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

                    // Device-category subdirectories avoid relying on AGP's occasionally incorrect parent directory.
                    listOf(intermediateOutputs, generatedOutputs).forEach { outputRoot ->
                        from(outputRoot) {
                            include("**/$directoryName/*.png")
                            eachFile { path = name }
                            includeEmptyDirs = false
                        }
                    }
                    into(playStoreGraphics.dir(directoryName))
                }
            }

            tasks.register(STORE_SCREENSHOTS_TASK) {
                group = "publishing"
                description = "Captures deterministic Play Store phone and tablet screenshots."
                dependsOn(syncTasks)
            }
        }
    }

    private fun String.toTaskName(): String =
        split('-').joinToString(separator = "") { it.replaceFirstChar(Char::uppercaseChar) }

    private companion object {
        const val STORE_SCREENSHOTS_TASK = "storeScreenshots"
        const val SCREENSHOT_BUILD_TYPE = "screenshot"
        const val SCREENSHOT_TEST_CLASS =
            "com.w2sv.filenavigator.ui.screenshot.StoreScreenshotTest"

        val SCREENSHOT_DEVICES = linkedMapOf(
            "storePhoneApi35" to "Pixel 6",
            "storeTabletApi35" to "Nexus 7",
            "storeLargeTabletApi35" to "Pixel Tablet"
        )
        val SCREENSHOT_DIRECTORIES = listOf(
            "phone-screenshots",
            "tablet-screenshots",
            "large-tablet-screenshots"
        )
        val ANDROID_STUDIO_MODEL_PROPERTIES = listOf(
            "android.injected.build.model.v2",
            "android.injected.build.model.only",
            "android.injected.build.model.only.advanced",
            "android.injected.build.model.only.versioned"
        )
    }
}
