package com.w2sv.filenavigator.benchmarking

import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigatorSettingsScreenBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun benchmarkCompilationNone() =
        navigatorSettingsInitialComposition(CompilationMode.DEFAULT)

    fun navigatorSettingsInitialComposition(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = "com.w2sv.filenavigator",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 2,
            setupBlock = {
                println("packageName=$packageName")
                pressHome()
            },
            measureBlock = {
                val intent = Intent()
                    .setClassName(packageName, "com.w2sv.filenavigator.NavigatorSettingsBenchmarkActivity")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityAndWait(intent)
            }
        )
    }
}
