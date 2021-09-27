package com.jitendraalekar.sock8.data.network

import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface ISocketManager {

    fun connect(onConnected : (() -> Unit)? = null)

    fun emit(eventName: String,  arg: String)

    fun disconnect()

    fun addListener(eventName : String, listener : Emitter.Listener)

    fun removeListener(eventName: String)

    fun removeAllListeners()

    fun connectToEvent(eventName: String ) : Flow<JSONObject>
}