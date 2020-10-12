package com.jon.common.service;

interface ThreadErrorListener {
    fun onThreadError(throwable: Throwable);
}
