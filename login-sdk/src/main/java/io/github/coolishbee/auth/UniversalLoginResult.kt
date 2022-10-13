package io.github.coolishbee.auth

import android.os.Parcelable
import io.github.coolishbee.ApiError
import io.github.coolishbee.ApiErrorCode
import io.github.coolishbee.ApiResponseCode
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UniversalLoginResult(
    var responseCode: ApiResponseCode,
    var socialProfile: UniversalProfile?,
    var errorData: ApiError
): Parcelable {

    companion object {

        private fun error(errorData: ApiError
        ): UniversalLoginResult {
            return Builder()
                .responseCode(ApiResponseCode.FAILED)
                .errData(errorData)
                .build()
        }

        fun internalError(errorMsg: String): UniversalLoginResult {
            return error(ApiError(ApiErrorCode.NOT_DEFINED, errorMsg))
        }

        fun canceledError(): UniversalLoginResult {
            return error(ApiError.createApiCancel())
        }

        fun authenticationAgentError(errorMsg: String): UniversalLoginResult {
            return error(ApiError(ApiErrorCode.AUTHENTICATION_AGENT_ERROR, errorMsg))
        }
    }

    data class Builder(
        var responseCode: ApiResponseCode = ApiResponseCode.SUCCESS,
        var socialProfile: UniversalProfile? = null,
        var errorData: ApiError = ApiError(ApiErrorCode.NOT_DEFINED, "")
    ) {
        fun responseCode(responseCode: ApiResponseCode) = apply {
            this.responseCode = responseCode
        }
        fun socialProfile(socialProfile: UniversalProfile?) = apply {
            this.socialProfile = socialProfile
        }
        fun errData(errorData: ApiError) = apply {
            this.errorData = errorData
        }
        fun build() = UniversalLoginResult(responseCode, socialProfile, errorData)
    }
}
