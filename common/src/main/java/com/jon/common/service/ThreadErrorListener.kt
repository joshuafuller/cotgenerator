package com.jon.common.service;

interface ThreadErrorListener {
    fun reportError(throwable: Throwable);
}
