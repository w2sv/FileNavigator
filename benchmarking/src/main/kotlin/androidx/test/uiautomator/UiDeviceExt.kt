package androidx.test.uiautomator

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
