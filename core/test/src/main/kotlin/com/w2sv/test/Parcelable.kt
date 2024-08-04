package com.w2sv.test

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals

/**
 * Requires @RunWith(RobolectricTestRunner::class) annotation on calling test class.
 */
inline fun <reified T : Parcelable> T.testParceling(flags: Int = 0) {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, flags)

    parcel.setDataPosition(0)

    assertEquals(this, parcelableCreator<T>().createFromParcel(parcel))
}