package io.github.coolishbee.api

import android.app.Activity

interface LoginApiClient {

    fun logout(activity: Activity)
}