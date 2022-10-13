package io.github.coolishbee

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApiError(
    var errCode: ApiErrorCode,
    var message: String?
) : Parcelable {

    companion object {

        fun createApiError(errCode: ApiErrorCode,
                           errString: String?): ApiError
        {
            return ApiError(errCode = errCode, message = errString)
        }

        fun createApiCancel(): ApiError
        {
            return ApiError(ApiErrorCode.CANCEL, "User Canceled")
        }
    }
}