package com.jon.common.service

import android.app.Notification

interface INotificationGenerator {
    fun getForegroundNotification(): Notification
}
