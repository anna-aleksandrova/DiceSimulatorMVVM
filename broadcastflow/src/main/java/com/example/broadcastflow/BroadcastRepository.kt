package com.example.broadcastflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BroadcastEvent(
    val id: String,
    val type: String,
    val timestamp: String,
    val details: String
)

class BroadcastRepository(private val context: Context) {

    fun observeEvents(): Flow<BroadcastEvent> = callbackFlow {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                val time = formatter.format(Date())
                val details = buildDetails(action, intent)
                trySend(
                    BroadcastEvent(
                        id = UUID.randomUUID().toString(),
                        type = action.substringAfterLast('.'),
                        timestamp = time,
                        details = details
                    )
                )
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_HEADSET_PLUG)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private fun buildDetails(action: String, intent: Intent): String {
        return when (action) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                val state = intent.getBooleanExtra("state", false)
                "Action: $action\nstate: $state"
            }
            Intent.ACTION_HEADSET_PLUG -> {
                val state = intent.getIntExtra("state", -1)
                val name = intent.getStringExtra("name") ?: "unknown"
                "Action: $action\nstate: $state\nname: $name"
            }
            Intent.ACTION_BATTERY_LOW -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                "Action: $action\nlevel: $level"
            }
            else -> "Action: $action"
        }
    }
}
