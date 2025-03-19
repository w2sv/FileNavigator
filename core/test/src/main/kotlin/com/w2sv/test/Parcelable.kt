package com.w2sv.test

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals

/**
 * Parcels the receiver, recreates it from the parcel and asserts whether the original instance and the recreated one are equal.
 *
 * Requires `@RunWith(RobolectricTestRunner::class)` annotation on calling test class.
 */
inline fun <reified T : Parcelable> T.testParceling(flags: Int = 0) {
    val parcel = Parcel.obtain()
    this.writeToParcel(parcel, flags)

    // Reset the parcel's position for reading
    parcel.setDataPosition(0)

    // Assert that the original and recreated objects are equal
    val recreated = parcelableCreator<T>().createFromParcel(parcel)
    assertEquals(this, recreated)

    // Recycle the parcel to avoid memory leaks
    parcel.recycle()
}
