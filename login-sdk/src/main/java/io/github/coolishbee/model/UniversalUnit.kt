package io.github.coolishbee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniversalUnit (
    val code: Int,
    val msg: String?
): Parcelable