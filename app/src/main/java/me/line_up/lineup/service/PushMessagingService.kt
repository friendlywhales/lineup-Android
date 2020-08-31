package me.line_up.lineup.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.line_up.lineup.App
import me.line_up.lineup.events.PushIncomingEvent
import me.line_up.lineup.events.PushTokenEvent
import java.lang.Exception

class PushMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            // val data = remoteMessage.data
            // {random=0.32243786663326146, something=important, age=27}
        }

        Log.d(App.TAG, "onMessageReceived: $remoteMessage")
        remoteMessage.messageId?.let {
            App.shared.bus.post(PushIncomingEvent(it))
        }
    }

    override fun onSendError(reason: String, error: Exception) {
        super.onSendError(reason, error)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(App.TAG, "newToken: $token")
        App.shared.bus.post(PushTokenEvent(token))
    }
}
