package com.jon.cotbeacon.chat.runnables

import android.content.SharedPreferences
import com.jon.common.repositories.ISocketRepository
import com.jon.common.service.SocketFactory
import com.jon.cotbeacon.chat.IChatRepository
import timber.log.Timber
import java.net.SocketException

abstract class ChatRunnable(
        protected val prefs: SharedPreferences,
        protected val socketRepository: ISocketRepository,
        protected val chatRepository: IChatRepository,
) : Runnable {

    protected open fun safeInitialise(initialisation: () -> Unit): Any? {
        return try {
            initialisation()
            Any()
        } catch (t: Throwable) {
            Timber.e(t)
            chatRepository.postError(t.message ?: "Unknown exception")
            null
        }
    }

    /**
     * Utility function which catches a Throwable, reports them to the chat repository
     * and returns null. Returns a non-null value if the passed lambda function completes
     * successfully.
     *
     * Used as:
     *      postErrorIfThrowable {
     *          int x = 2
     *          int y = x * 2
     *          int z = x / 0
     *      } ?: doSomethingOnError()
     *
     * @param function lambda function to be run and verified.
     * @return null if a Throwable is caught, non-null otherwise.
     */
    protected fun postErrorIfThrowable(function: () -> Unit): Any? {
        return try {
            function()
            true
        } catch (e: SocketException) {
            /* Thrown when a socket is closed externally whilst listening, i.e. when the user
             * tells the service to shutdown. No-op, just back out and finish the runnable */
            null
        } catch (t: Throwable) {
            Timber.e(t)
            chatRepository.postError(t.message ?: "Unknown exception")
            null
        }
    }
}
