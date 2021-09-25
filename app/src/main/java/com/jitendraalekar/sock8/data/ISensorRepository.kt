package com.jitendraalekar.sock8.data

import com.google.gson.JsonObject
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface ISensorRepository {

    fun connect(onConnected : () -> Unit)

    fun disconnect()

    fun subscribeToSensor(sensorName : String)

    fun unSubscribeToSensor(sensorName : String)

    fun addListener(eventName : String, listener : (args : Array<out Any>) -> Unit)

    fun removeListener(eventName: String)

    fun removeAllListeners()

    suspend fun getSensorNames() : List<String>

    //could be map of sensor name and config
    suspend fun getSensorConfig() : JsonObject

     fun connectToDataStream() : Flow<JSONObject>

}