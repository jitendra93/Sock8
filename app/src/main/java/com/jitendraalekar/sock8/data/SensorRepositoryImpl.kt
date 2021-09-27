package com.jitendraalekar.sock8.data

import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.network.ISocketManager
import com.jitendraalekar.sock8.data.network.SensorEvents
import com.jitendraalekar.sock8.data.network.SensorService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject


private const val SUBSCRIBE = "subscribe"
private  const val UNSUBSCRIBE = "unsubscribe"

class SensorRepositoryImpl @Inject constructor(
    private val socketManager: ISocketManager,
    private val sensorService: SensorService,
    private val dispatcher: CoroutineDispatcher
) : ISensorRepository {



    override fun connect(onConnected : () -> Unit) {
        socketManager.connect(onConnected)
    }

    override fun disconnect() {
        socketManager.disconnect()
    }

    override fun subscribeToSensor(sensorName: String) {
        socketManager.emit(SUBSCRIBE,sensorName)
    }

    override fun unSubscribeToSensor(sensorName: String) {
        socketManager.emit(UNSUBSCRIBE,sensorName)
    }

    override fun addListener(eventName: String, listener: (args: Array<out Any>) -> Unit) {
        socketManager.addListener(eventName, listener)
    }

    override fun removeListener(eventName: String) {
        socketManager.removeListener(eventName)
    }

    override fun removeAllListeners() {
        socketManager.removeAllListeners()
    }

    override suspend fun getSensorNames(): List<String> {

        return sensorService.getSensorNames()
    }

    override suspend fun getSensorConfig(): JsonObject {
        return sensorService.getSensorConfig()
    }

    override fun connectToDataStream(): Flow<JSONObject> {
         return socketManager.connectToEvent(SensorEvents.DATA.toString())
    }

}