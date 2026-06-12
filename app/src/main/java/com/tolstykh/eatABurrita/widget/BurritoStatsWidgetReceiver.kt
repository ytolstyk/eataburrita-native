package com.tolstykh.eatABurrita.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BurritoStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BurritoStatsWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleStatAdvance(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.getSystemService(AlarmManager::class.java)
            ?.cancel(buildPendingIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            // Restart the alarm chain on every system widget update (fires every 30 min
            // and when the widget is placed on the home screen). This auto-recovers the
            // chain after an APK reinstall without requiring the user to re-add the widget.
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> scheduleStatAdvance(context)

            ACTION_ADVANCE_STAT -> {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        // statIndex is computed from wall time inside provideGlance, so
                        // all we need to do here is trigger a redraw on each instance.
                        val widget = BurritoStatsWidget()
                        GlanceAppWidgetManager(context)
                            .getGlanceIds(BurritoStatsWidget::class.java)
                            .forEach { id -> widget.update(context, id) }
                        scheduleStatAdvance(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_ADVANCE_STAT = "com.tolstykh.eatABurrita.ADVANCE_STAT"

        fun scheduleStatAdvance(context: Context) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            am.setWindow(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 5_000L,
                1_000L,
                buildPendingIntent(context),
            )
        }

        private fun buildPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, BurritoStatsWidgetReceiver::class.java).apply {
                action = ACTION_ADVANCE_STAT
            }
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_ADVANCE_STAT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private const val REQUEST_CODE_ADVANCE_STAT = 1001
    }
}
