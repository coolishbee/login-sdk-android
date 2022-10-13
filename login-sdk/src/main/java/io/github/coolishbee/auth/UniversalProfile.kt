package io.github.coolishbee.auth

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniversalProfile(
    val userID: String?,
    val idToken: String?,
    val displayName: String?,
    val photoURL: String?,
    val email: String?
): Parcelable
