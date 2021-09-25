package com.jitendraalekar.sock8.ui

import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.*
import com.jitendraalekar.sock8.domain.ConnectSensorDataStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import com.jitendraalekar.sock8.Result
import com.jitendraalekar.sock8.domain.SubscribeSensorUseCase
import com.jitendraalekar.sock8.domain.UnSubscribeSensorUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val repository: SensorRepositoryImpl,
    private val gson: Gson,
    private val connectSensorDataStreamUseCase: ConnectSensorDataStreamUseCase,
    private val subscribeSensorUseCase: SubscribeSensorUseCase,
    private val unSubscribeSensorUseCase: UnSubscribeSensorUseCase
) : ViewModel() {
    //todo handle disconnection
    private val mutex: Mutex = Mutex()
    private val _dataMap: MutableLiveData<Map<String, SensorData>> = MutableLiveData(mapOf())
    val dataMap: LiveData<Map<String, SensorData>> by ::_dataMap

    private val _sensorsState: MutableLiveData<Map<String, Boolean>> = MutableLiveData()
    val sensorsState: LiveData<Map<String, Boolean>> = _sensorsState

    var sensorConfigMap: Map<String, SensorConfig>? = null

    init {
        preload()
    }

    fun preload() {
        Timber.d("Preloading")
        viewModelScope.launch(dispatcher) {
            //todo handle exceptions
            var sensorNames = emptyList<String>()
            launch {
                sensorNames = repository.getSensorNames()
                Timber.d("Preloading - sensor names = ${sensorNames.joinToString("-")}")
            }

            var config = JsonObject()
            launch {
                config = repository.getSensorConfig()
            }.join()

            sensorNames.map {
                Pair(it, false)
            }.toMap().let {
                _sensorsState.postValue(it)
            }

            val map = mutableMapOf<String, SensorConfig>()
            sensorNames.forEach {
                val minMax = config[it].asJsonObject
                map[it] = SensorConfig(minMax["min"].asFloat, minMax["max"].asFloat)
            }
            sensorConfigMap = map

        }
        viewModelScope.launch {


            /*    repository.connect {
                    repository.addListener("data"){
                        println(it[0].toString())
                    }
                }*/

        }
    }

    fun subscribeToSensor(sensorName: String) {
        viewModelScope.launch {
            val isSubscribed = sensorsState.value?.get(sensorName) == true
            _sensorsState.postValue(_sensorsState.value!!.toMutableMap().apply {
                this[sensorName] = !isSubscribed
            })
            if (isSubscribed) {
                unSubscribeSensorUseCase(sensorName)
            } else {
                subscribeSensorUseCase(sensorName)
            }
        }
    }


    fun loadSensorData(sensorName: String) {

        viewModelScope.launch {
            connectSensorDataStreamUseCase(sensorName).collect { message ->
                println(message)
                mutex.withLock {
                    if (message is Result.Success) {
                        when (message.data) {
                            is Init -> {

                                try {
                                    Timber.d("init ${message.data.sensorName}")
                                    Timber.d("sensor config map ${sensorConfigMap}")
                                    Timber.d(
                                        "sensor config map value contains  ${
                                            sensorConfigMap?.containsKey(
                                                message.data.sensorName
                                            )
                                        }"
                                    )
                                    Timber.d(
                                        "sensor config map value get  ${
                                            sensorConfigMap?.get(
                                                message.data.sensorName
                                            )
                                        }"
                                    )
                                    _dataMap.value?.toMutableMap()?.let { map ->
                                        map[message.data.sensorName!!] = SensorData(
                                            sensorName = message.data.sensorName!!,
                                            recent = message.data.recent.asMap(),
                                            minute = message.data.minute.asMap(),
                                            sensorConfig = sensorConfigMap!!.get(message.data.sensorName)!!
                                        )
                                        _dataMap.postValue(map)
                                    }

                                } catch (e: Exception) {
                                    Timber.e(e)
                                }
                            }
                            is Update -> {
                                Timber.d("update ${message.data.sensorName}")

                                _dataMap.value?.toMutableMap()?.let { map ->
                                    val sensorData = map[message.data.sensorName]
                                    if (message.data.scale == Scale.RECENT) {
                                        val newRecentData = sensorData?.recent?.toMutableMap()
                                        newRecentData?.let {
                                            it.put(message.data.key, message.data.value)
                                            val newSensorData =
                                                sensorData.copy(recent = newRecentData.toMap())
                                            map[message.data.sensorName] = newSensorData

                                        }
                                    }
                                    if (message.data.scale == Scale.MINUTE) {
                                        val newMinuteData = sensorData?.minute?.toMutableMap()
                                        newMinuteData?.let {
                                            it.put(message.data.key, message.data.value)
                                            val newSensorData =
                                                sensorData.copy(minute = newMinuteData.toMap())
                                            map[message.data.sensorName] = newSensorData
                                        }
                                    }
                                    _dataMap.postValue(map)

                                }
                            }
                            is Delete -> {
                                Timber.d("delete ${message.data.sensorName}")

                                _dataMap.value?.toMutableMap()?.let { map ->
                                    val sensorData: SensorData? = map[message.data.sensorName]
                                    if (message.data.scale == Scale.RECENT) {
                                        val newRecentData = sensorData?.recent?.toMutableMap()
                                        newRecentData?.let {
                                            it.remove(message.data.key)
                                            val newSensorData =
                                                sensorData.copy(recent = newRecentData.toMap())
                                            map[message.data.sensorName] = newSensorData
                                        }
                                    }
                                    if (message.data.scale == Scale.MINUTE) {
                                        val newMinuteData = sensorData?.minute?.toMutableMap()
                                        newMinuteData?.let {
                                            it.remove(message.data.key)
                                            val newSensorData =
                                                sensorData.copy(recent = newMinuteData.toMap())
                                            map[message.data.sensorName] = newSensorData
                                        }
                                    }
                                    _dataMap.postValue(map)
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    override fun onCleared() {
        viewModelScope.coroutineContext.cancelChildren()
        repository.disconnect()
        repository.removeAllListeners()
        super.onCleared()
    }

}