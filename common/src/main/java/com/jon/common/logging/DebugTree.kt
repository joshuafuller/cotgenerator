package com.jon.common.logging

import timber.log.Timber

open class DebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(" + element.fileName + ":" + element.lineNumber + ")"
    }
}
