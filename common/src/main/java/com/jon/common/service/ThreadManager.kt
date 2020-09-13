package com.jon.common.service

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import java.util.concurrent.Executor
import java.util.concurrent.Executors


internal class ThreadManager(private val prefs: SharedPreferences, private val errorListener: ThreadErrorListener) : OnSharedPreferenceChangeListener {
    private var thread: BaseThread? = null
    private val exceptionHandler = Thread.UncaughtExceptionHandler { _, throwable: Throwable -> errorListener.reportError(throwable) }
    val isRunning: Boolean
        get() = thread?.isRunning() ?: false

    fun start() {
        prefs.registerOnSharedPreferenceChangeListener(this)
        thread = BaseThread.fromPrefs(prefs).apply {
            uncaughtExceptionHandler = exceptionHandler
            start()
        }
    }

    fun shutdown() {
        thread?.let {
            val executor: Executor = Executors.newSingleThreadExecutor()
            executor.execute {
                it.shutdown()
                thread = null
            }
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        /* If any preferences are changed, kill the thread and instantly reload with the new settings */
        if (isRunning) {
            shutdown()
            start()
        }
    }
}
