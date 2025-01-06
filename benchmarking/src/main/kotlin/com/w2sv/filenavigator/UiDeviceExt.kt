package com.w2sv.filenavigator

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import java.io.ByteArrayOutputStream

private fun UiDevice.dumpWindowHierarchy(): String {
    val outputStream = ByteArrayOutputStream()
    dumpWindowHierarchy(outputStream)
    return outputStream.toString("UTF-8")
}

private fun UiDevice.flingListDown(resourceName: String) {
    findObject(By.res(resourceName)).fling(Direction.DOWN)
    waitForIdle()
}
