package me.line_up.lineup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi
import com.squareup.otto.Subscribe
import me.line_up.lineup.events.PushIncomingEvent
import me.line_up.lineup.events.PushTokenEvent

class MainActivity : AppCompatActivity() {
    lateinit var mainWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        App.shared.bus.register(this)

        mainWebView = createWebView()
        setContentView(mainWebView)
//        setContentView(TextView(this).apply {
//            text = "None"
//        })
//        val intent = Intent(this, PostActivity::class.java)
//        intent.putExtra("token", "1234")
//        startActivity(intent)
//        overridePendingTransition(R.anim.slide_enter, R.anim.slide_still)
    }

    @Subscribe
    fun handlePushToken(event: PushTokenEvent) {
        runOnUiThread {
            registerDeviceToken(mainWebView, event.newToken)
        }
    }

    @Subscribe
    fun handlePushNotification(@Suppress("UNUSED_PARAMETER") event: PushIncomingEvent) {
        runOnUiThread {
            mainWebView.loadUrl("${App.baseURL}/notifications/followings", createCommonHeader())
        }
    }

    override fun onDestroy() {
        App.shared.bus.unregister(this)
        super.onDestroy()
    }

    private fun createWebView(): WebView {
//        WebView.setWebContentsDebuggingEnabled(true)

        val webview = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            addJavascriptInterface(WebAppInterface(context), "Lineup")
        }
        webview.webChromeClient = object: WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                presentPostScreen(webview, "lineup://post?token=n/a")
                return false
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage != null) {
//                    Log.i(App.TAG, "JS-Console: ${consoleMessage.message()}")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }
        webview.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                App.shared.getCurrentPushToken { token ->
                    if (token != null) {
                        registerDeviceToken(webview, token)
                    }
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.startsWith("lineup")) {
                        presentPostScreen(webview, url)
                        return true
                    }
                }
                @Suppress("DEPRECATION")
                return super.shouldOverrideUrlLoading(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.url?.scheme == "lineup") {
                    presentPostScreen(webview, request.url.toString())
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (error != null) {
                    Log.e(App.TAG, "onReceivedError: code=${error.errorCode}")
                }
            }
        }

        webview.loadUrl("${App.baseURL}", createCommonHeader())
        return webview
    }

    private fun createCommonHeader(): Map<String, String> {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appVersion = packageInfo.versionName
        return mapOf(
            "X-App-Version" to appVersion,
            "X-OS-Type" to "Android",
            "X-OS-Version" to "${Build.VERSION.SDK_INT}"
        )
    }

    private fun registerDeviceToken(webView: WebView, pushToken: String) {
        fakeNavigator(webView)
        Log.e(App.TAG, "PushToken: $pushToken")
        webView.evaluateJavascript("""
            localStorage.setItem('deviceToken', '${pushToken}');
            localStorage.setItem('deviceName', 'android');
        """.trimIndent()) { }
    }

    private fun fakeNavigator(webView: WebView) {
        val fakeUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1"
        webView.evaluateJavascript("""
        if (navigator.__defineGetter__) {
            navigator.__defineGetter__('userAgent', function() {
                return "$fakeUserAgent";
            });
            navigator.__defineGetter__('platform', function() {
                return "$fakeUserAgent";
            });
        } else if (Object.defineProperty) {
            Object.defineProperty(navigator, 'userAgent', {
                get: function() { return "$fakeUserAgent"; }
            });
            Object.defineProperty(navigator, 'platform', {
                get: function() { return "$fakeUserAgent"; }
            });
        }
        """.trimIndent()) { }
    }

    private fun presentPostScreen(webView: WebView,
                                  @Suppress("UNUSED_PARAMETER") referenceUrl: String) {
        webView.evaluateJavascript("localStorage.getItem('token')") {
            val token = it.replace("\"", "")
            Log.d(App.TAG, "* presentPostScreen with $token")

            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_still)
        }
    }
}

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun showToast() {
    }
}
