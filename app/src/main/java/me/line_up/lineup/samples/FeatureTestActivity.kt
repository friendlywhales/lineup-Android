package me.line_up.lineup.samples

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import me.line_up.lineup.WebAppInterface

class FeatureTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this

        val webview = WebView(this).apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(WebAppInterface(context), "Lineup")
            loadUrl("https://limitedition.kr/tmp/test.html")
        }
        val uploadButton = Button(this).apply {
            text = "Test Upload"
            setOnClickListener {
                SampleUploadImage().testUpload(context)
            }
        }
        val galleryButton = Button(this).apply {
            text = "Image Chooser"
            setOnClickListener {
                // TODO: Pop image Chooser
            }
        }
        val bridgeButton = Button(this).apply {
            text = "Call javascript"
            setOnClickListener {
                webview.loadUrl("javascript:shoot()")
            }
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(uploadButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 0.0f))
        layout.addView(galleryButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 0.0f))
        layout.addView(bridgeButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 0.0f))
        layout.addView(webview, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1.0f))

        setContentView(layout)
    }
}

class WebAppTestInterface(private val context: Context) {
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }
}