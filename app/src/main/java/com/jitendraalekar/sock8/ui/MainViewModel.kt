package com.jitendraalekar.sock8.ui

import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.*
import com.jitendraalekar.sock8.domain.ConnectSensorDataStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jitendraalekar.sock8.Result
import com.jitendraalekar.sock8.domain.SubscribeSensorUseCase
import com.jitendraalekar.sock8.domain.UnSubscribeSensorUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val repository: SensorRepositoryImpl,
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
    var newStreamForSensor : String? = null
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
            loadSensorData("")
        }
    }


    fun updated(listOfSensors : Map<String, Boolean>) {

        listOfSensors.filter {
            it.value != _sensorsState.value?.get(it.key)
        }.forEach {
            toggleSubscription(it.key,it.value)
        }
       _sensorsState.value = listOfSensors
    }

    fun toggleSubscription(sensorName: String, subscribe : Boolean) {
        viewModelScope.launch {

            if (subscribe) {

                newStreamForSensor = sensorName
                subscribeSensorUseCase(sensorName)

            } else {

                unSubscribeSensorUseCase(sensorName)
                _dataMap.value = _dataMap.value?.toMutableMap()?.apply {
                    remove(sensorName)
                }
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
                                newStreamForSensor?.let {
                                    message.data.sensorName = it
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
                                }
                                newStreamForSensor = null
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