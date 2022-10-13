package io.github.coolishbee.api

import android.app.Activity
import io.github.coolishbee.auth.UniversalLoginApi

class LoginApiClientImpl(
    activity: Activity
) : LoginApiClient {

    override fun logout(activity: Activity) {
        UniversalLoginApi.googleLogout(activity)
    }
}