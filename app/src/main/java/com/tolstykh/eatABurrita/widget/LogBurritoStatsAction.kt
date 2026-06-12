package com.tolstykh.eatABurrita.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.tolstykh.eatABurrita.data.BurritoEntry
import dagger.hilt.android.EntryPointAccessors

class LogBurritoStatsAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .burritoDao()
        dao.insert(BurritoEntry(timestamp = System.currentTimeMillis()))
        BurritoStatsWidget().update(context, glanceId)
    }
}
