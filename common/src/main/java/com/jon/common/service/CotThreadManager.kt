package com.jon.common.service

import android.content.SharedPreferences
import com.jon.common.repositories.ISocketRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class CotThreadManager(
        prefs: SharedPreferences,
        private val cotFactory: CotFactory,
        private val errorListener: IThreadErrorListener,
        private val socketRepository: ISocketRepository
) : ThreadManager(prefs), SharedPreferences.OnSharedPreferenceChangeListener {

    private var thread: BaseThread? = null

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun start() {
        thread = BaseThread.fromPrefs(prefs, socketRepository, cotFactory).apply {
            setUncaughtExceptionHandler { _, t: Throwable -> errorListener.onThreadError(t) }
            start()
        }
    }

    override fun shutdown() {
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
        if (isRunning()) {
            shutdown()
            start()
        }
    }

    override fun isRunning(): Boolean {
        return thread?.isRunning() ?: false
    }
}
