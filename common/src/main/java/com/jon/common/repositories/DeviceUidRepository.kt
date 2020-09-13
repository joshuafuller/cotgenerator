package com.jon.common.repositories

import android.content.Context
import com.jon.common.CotApplication
import timber.log.Timber
import java.io.*
import java.util.*

class DeviceUidRepository {
    val uid = generate(CotApplication.context)

    private fun generate(context: Context): String {
        var generatedUid: String
        try {
            val file = File(context.filesDir, FILENAME)
            if (file.exists()) {
                Timber.i("Reading UID from file...")
                generatedUid = readPreviousUid(context)
                Timber.i("Successfully read UID %s", uid)
            } else {
                Timber.i("Writing new UID to file...")
                generatedUid = createAndWriteNewUid(context)
                Timber.i("Successfully written UID %s", uid)
            }
        } catch (e: IOException) {
            generatedUid = UUID.randomUUID().toString()
            Timber.e(e)
            Timber.e("Failed to read/write UID from/to file. Using a temporary one instead: %s", uid)
        }
        return generatedUid
    }

    @Throws(IOException::class)
    private fun readPreviousUid(context: Context): String {
        val inputStream: InputStream = context.openFileInput(FILENAME)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val line = bufferedReader.readLine()
        inputStream.close()
        return line
    }

    @Throws(IOException::class)
    private fun createAndWriteNewUid(context: Context): String {
        val uuid = UUID.randomUUID().toString()
        val fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(uuid.toByteArray())
        fos.close()
        return uuid
    }

    companion object {
        private val instance = DeviceUidRepository()
        fun getInstance() = instance

        private const val FILENAME = "uuid.txt"
    }
}
