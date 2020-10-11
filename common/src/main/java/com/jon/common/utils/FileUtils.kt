package com.jon.common.utils

import androidx.annotation.RawRes
import com.jon.common.CotApplication
import org.apache.commons.io.IOUtils
import java.io.FileInputStream

object FileUtils {
    fun toByteArray(filepath: String): ByteArray {
        return IOUtils.toByteArray(
                FileInputStream(filepath)
        )
    }

    fun rawResourceToByteArray(@RawRes resId: Int): ByteArray {
        val inputStream = CotApplication.context.resources.openRawResource(resId)
        return IOUtils.toByteArray(inputStream)
    }
}
