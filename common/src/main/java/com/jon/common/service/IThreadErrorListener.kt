package com.jon.common.service;

interface IThreadErrorListener {
    fun onThreadError(throwable: Throwable);
}
