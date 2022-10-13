package io.github.coolishbee.api

import android.app.Activity

class LoginApiClientBuilder(
    private val activity: Activity
) {
    private val context = activity.applicationContext

    fun build(): LoginApiClient {
        return LoginApiClientImpl(
            activity
        )
    }


}