package com.w2sv.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals

internal inline fun <reified T : Parcelable> T.testParceling(
    flags: Int = 0
): T {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, flags)

    parcel.setDataPosition(0)

    val deparceled = parcelableCreator<T>().createFromParcel(parcel)

    assertEquals(this, deparceled)

    return deparceled
}