package com.jon.common.utils

import android.content.Context
import androidx.annotation.RawRes
import org.apache.commons.io.IOUtils
import java.io.FileInputStream

object FileUtils {
    fun toByteArray(filepath: String): ByteArray {
        return IOUtils.toByteArray(
                FileInputStream(filepath)
        )
    }

    fun rawResourceToByteArray(context: Context, @RawRes resId: Int): ByteArray {
        val inputStream = context.resources.openRawResource(resId)
        return IOUtils.toByteArray(inputStream)
    }
}
