package com.w2sv.test

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals

inline fun <reified T : Parcelable> T.testParceling(flags: Int = 0) {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, flags)

    parcel.setDataPosition(0)

    val deparceled = parcelableCreator<T>().createFromParcel(parcel)

    assertEquals(this, deparceled)
}