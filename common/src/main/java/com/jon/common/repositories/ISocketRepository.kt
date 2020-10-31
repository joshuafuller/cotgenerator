package com.jon.common.repositories

import java.io.OutputStream
import java.net.MulticastSocket
import java.net.Socket
import javax.net.ssl.SSLSocket

interface ISocketRepository {
    fun getUdpInputSocket(group: String, port: Int): MulticastSocket
    fun getTcpSocket(): Socket
    fun getSslSocket(): SSLSocket
    fun getOutputStream(socket: Socket): OutputStream
}
