package io.github.orioneee.processors

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import io.github.orioneee.NotificationInfo
import io.github.orioneee.axer.R
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.koin.IsolatedContext
import io.github.orioneee.presentation.AxerUIEntryPoint
import io.github.orioneee.presentation.navigation.FlowDestinations

internal actual fun notifyAboutException(exception: AxerException) {
    val isEnabledRequests = AxerUIEntryPoint.availableDestinations.contains(FlowDestinations.EXCEPTIONS_FLOW)
    if (!isEnabledRequests) {
        return
    }
    val context: Context by IsolatedContext.koin.inject()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        /* id = */ NotificationInfo.CHANNEL_ID,
        /* name = */ NotificationInfo.CHANNEL_NAME,
        /* importance = */ NotificationManager.IMPORTANCE_LOW
    )
    notificationManager.createNotificationChannel(channel)
    val notificationText = exception.shortName

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        NotificationInfo.getLaunchIntent(context),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, NotificationInfo.CHANNEL_ID)
        .setContentTitle("Recorded new exception")
        .setContentText(notificationText)
        .setSmallIcon(R.drawable.bug_report)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(exception.id.toInt(), notification)
}