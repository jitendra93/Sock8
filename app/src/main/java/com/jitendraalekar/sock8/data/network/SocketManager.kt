package com.jitendraalekar.sock8.data.network

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketManager @Inject constructor() : ISocketManager {


    private val socket: Socket = IO.socket(BASE_URL)


    @ExperimentalCoroutinesApi
    override fun connectToEvent(eventName: String) = callbackFlow {

        val callback = Emitter.Listener { args ->
            try {
                println("socket id ${socket.id()}")
                (args[0] as? JSONObject)?.let { offer(it) }
            } catch (t: Throwable) {
                Timber.d("Exception in offer $t")
            }
        }

        try {
            Timber.d("Starting to listen")
            if (!(socket.connected() || socket.io().isReconnecting)) {
                connect {
                    socket.on(eventName, callback)
                    Timber.d("Adding new event[$eventName] callback")
                }
            } else {
                Timber.d("Socket is already connected or reconnecting")
            }

        } catch (t: Throwable) {
            Timber.d("Exception in callback flow $t")
        }
        awaitClose {
            socket.off("data")
            socket.disconnect()
            Timber.d("Requesting to remove subscription for event - $eventName")
        }
    }

    override fun connect(onConnected: (() -> Unit)?) {
        socket.connect()
        Timber.d("Requesting socket connection")
        socket.once(Socket.EVENT_CONNECT) {
            onConnected?.invoke()
            Timber.d("Successfully Connected ${socket.connected()} socket id - ${socket.id()}")
        }
    }

    override fun emit(eventName: String, arg: String) {
        socket.emit(eventName, arg)
        Timber.d("Emitting with socket-id(${socket.id()}) connected =${socket.connected()} $eventName $arg")
    }

    override fun disconnect() {
        socket.disconnect()
        Timber.d("Requesting socket disconnection")
    }

    override fun addListener(eventName: String, listener: Emitter.Listener) {
        if (!socket.connected())
            socket.connect()
        socket.on(eventName, listener)
        Timber.d("Add listener to $eventName")
    }

    override fun removeListener(eventName: String) {
        socket.off(eventName)
        Timber.d("Remove listener for $eventName")

    }

    override fun removeAllListeners() {
        socket.off()
        Timber.d("Requesting removal of all listeners")

    }
}