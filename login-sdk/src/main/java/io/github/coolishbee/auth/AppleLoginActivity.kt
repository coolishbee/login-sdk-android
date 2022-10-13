package io.github.coolishbee.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.github.coolishbee.LoginType
import io.github.coolishbee.R
import io.github.coolishbee.utils.UniversalUtils
import java.io.UnsupportedEncodingException

class AppleLoginActivity : Activity() {

    private var webView: WebView? = null
    private var state: String? = null
    private var progressBar: ProgressBar? = null

    private var appleClientId: String? = null
    private var redirectURL: String? = null

    private val APPLE_SCOPE = "name%20email"
    private val APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appleClientId = this.resources.getString(R.string.apple_client_id)

        if (TextUtils.isEmpty(appleClientId)) {
            onAuthenticationFinished(
                UniversalLoginResult.internalError("The apple_client_id is empty.")
            )
            return
        }

        redirectURL = this.resources.getString(R.string.redirect_url)

        if (TextUtils.isEmpty(redirectURL)) {
            onAuthenticationFinished(
                UniversalLoginResult.internalError("The apple login redirect_url is empty.")
            )
            return
        }

        setContentView(R.layout.sdk_webview_apple_login)
        webView = findViewById(R.id.apple_login_webView)
        progressBar = findViewById(R.id.apple_login_progressBar)

        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.webChromeClient = WebChromeClient()
        webView?.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar?.visibility = View.GONE
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                //return super.shouldOverrideUrlLoading(view, request)
                return isUrlOverridden(view, request?.url)
            }
        }
        state = UniversalUtils.getUUID()
        val rawNonce = UniversalUtils.sha256(UniversalUtils.generateNonce(32))
//        val url = (APPLE_AUTH_URL
//                + "?response_type=code%20id_token&v=1.1.6&response_mode=form_post&client_id="
//                + appleClientId
//                + "&scope=" + APPLE_SCOPE
//                + "&state=" + state
//                + "&nonce=" + rawNonce
//                + "&redirect_uri=" + redirectURL)

        val uri = Uri.parse(APPLE_AUTH_URL)
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("v", "1.1.6")
            .appendQueryParameter("response_mode", "form_post")
            .appendQueryParameter("client_id", appleClientId)
            .appendQueryParameter("scope", APPLE_SCOPE)
            .appendQueryParameter("state", state)
            .appendQueryParameter("nonce", rawNonce)
            .appendQueryParameter("redirect_uri", redirectURL)
            .build()

        webView?.loadUrl(uri.toString())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView?.canGoBack() == true) {
            webView?.goBack()
            return true
        } else {
            onAuthenticationFinished(UniversalLoginResult.canceledError())
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun onAuthenticationFinished(loginResult: UniversalLoginResult) {
        val resultData = Intent()
        resultData.putExtra(RESPONSE_DATA_KEY_APPLE_AUTH_RESULT, loginResult)
        setResult(LoginType.APPLE.ordinal, resultData)
        finish()
    }

    private fun jwtDecoded(JWTEncoded: String): String {
        var decodedJson = ""
        try {
            val split = JWTEncoded.split("\\.".toRegex()).toTypedArray()
            Log.d(TAG, "Header: " + getJson(split[0]))
            Log.d(TAG, "Body: " + getJson(split[1]))
            decodedJson = getJson(split[1])
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return decodedJson
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, charset("UTF-8"))
    }

    private fun isUrlOverridden(view: WebView?, url: Uri?): Boolean {
        return when {
            url == null -> {
                false
            }
            url.toString().contains("appleid.apple.com") -> {
                view?.loadUrl(url.toString())
                true
            }
            url.toString().contains(redirectURL!!) -> {
                val codeParam = url.getQueryParameter("code")
                val stateParam = url.getQueryParameter("state")
                val idTokenParam = url.getQueryParameter("id_token")
                val userParam = url.getQueryParameter("user")

                when {
                    codeParam == null -> {
                        Log.d(TAG, "code not returned")
                        Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    stateParam != state -> {
                        Log.d(TAG, "state does not match")
                        Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        if(userParam != null)
                            Log.d(TAG, userParam)
                        if(idTokenParam != null)
                            jwtDecoded(idTokenParam).let { Log.d(TAG, it) }

                        Toast.makeText(applicationContext, "로그인 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                true
            }
            else -> {
                false
            }
        }
    }

    companion object {
        private const val TAG = "AppleLoginActivity"
        private const val RESPONSE_DATA_KEY_APPLE_AUTH_RESULT = "apple_auth_result"

        fun getLoginIntent(
            context: Context
        ): Intent {
            return Intent(context, AppleLoginActivity::class.java)
        }

        fun getResultFromIntent(intent: Intent): UniversalLoginResult {
            val loginResult: UniversalLoginResult? =
                intent.getParcelableExtra(RESPONSE_DATA_KEY_APPLE_AUTH_RESULT)
            return loginResult ?: UniversalLoginResult.authenticationAgentError(
                "Authentication result is not found.")
        }
    }
}