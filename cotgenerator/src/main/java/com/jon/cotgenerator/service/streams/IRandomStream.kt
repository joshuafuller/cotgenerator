package com.jon.cotgenerator.service.streams

interface IRandomStream<T> {
    fun next(): T
}
