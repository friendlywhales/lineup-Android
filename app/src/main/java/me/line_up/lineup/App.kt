package me.line_up.lineup

import android.app.Application
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.otto.Bus
import com.squareup.otto.ThreadEnforcer

class App : Application() {
    var density: Float = 1.0f
    lateinit var bus: Bus

    override fun onCreate() {
        super.onCreate()

        this.density = this.resources.displayMetrics.density
        this.bus = Bus()

        shared = this

        FirebaseApp.initializeApp(this)
    }

    fun dp(value: Int): Int = (value.toFloat() * density).toInt()

    fun getCurrentPushToken(callback: (String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }
            callback(task.result?.token)
        })
    }

    companion object {
        lateinit var shared: App
        val TAG: String = "LineUp"
        val baseURL: String = "https://beta.line-up.me"
        val apiURL: String = "https://api.line-up.me/api/v1"
//        val baseURL: String = "https://staging.line-up.me"
//        val apiURL: String = "https://staging-api.line-up.me/api/v1"
    }

}
