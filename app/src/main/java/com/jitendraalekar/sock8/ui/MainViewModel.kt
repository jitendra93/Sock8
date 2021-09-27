package com.jitendraalekar.sock8.ui

import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jitendraalekar.sock8.Result
import com.jitendraalekar.sock8.domain.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dispatcher: CoroutineDispatcher,
    private val fetchSensorNamesUseCase: FetchSensorNamesUseCase,
    private val fetchSensorConfigUseCase: FetchSensorConfigUseCase,
    private val clearSocketConnectionsUseCase: ClearSocketConnectionsUseCase,
    private val connectSensorDataStreamUseCase: ConnectSensorDataStreamUseCase,
    private val subscribeSensorUseCase: SubscribeSensorUseCase,
    private val unSubscribeSensorUseCase: UnSubscribeSensorUseCase
) : ViewModel() {

    private val mutex: Mutex = Mutex()
    private val _dataMap: MutableLiveData<Map<String, SensorData>> = MutableLiveData(mapOf())
    val dataMap: LiveData<Map<String, SensorData>> by ::_dataMap

    private val _sensorsState: MutableLiveData<Map<String, Boolean>> = MutableLiveData()
    val sensorsState: LiveData<Map<String, Boolean>> = _sensorsState

    var isRecentSelected = false
    var sensorConfigMap: Map<String, SensorConfig>? = null
    var newStreamForSensor: String? = null

    init {
        preload()
    }

    fun preload() {
        viewModelScope.launch(dispatcher) {
            var sensorNames = emptyList<String>()
            launch {
                val result = fetchSensorNamesUseCase(Unit)
                sensorNames = when (result) {
                    is Result.Success -> result.data
                    else -> emptyList()
                }
            }

            var config = JsonObject()
            launch {
                val result = fetchSensorConfigUseCase(Unit)
                config = when (result) {
                    is Result.Success -> result.data
                    else -> JsonObject()
                }
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
            loadSensorData()
        }
    }

    fun requestSensorData(sensor: String, subscribe: Boolean) {
        toggleSubscription(sensor, subscribe)
        _sensorsState.value = _sensorsState.value?.toMutableMap().apply {
            this?.set(sensor, subscribe)
        }
    }


    fun toggleSubscription(sensorName: String, subscribe: Boolean) {
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


    fun loadSensorData() {

        viewModelScope.launch {
            connectSensorDataStreamUseCase(Unit).collect { message ->
                println(message)
                mutex.withLock {
                    if (message is Result.Success) {
                        when (message.data) {
                            is Init -> {
                                handleInitData(message.data)
                            }
                            is Update -> {
                                handleUpdateData(message.data)
                            }
                            is Delete -> {
                                handleDeleteData(message.data)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun handleDeleteData(data: Delete) {
        _dataMap.value?.toMutableMap()?.let { map ->
            val sensorData: SensorData? = map[data.sensorName]
            if (data.scale == Scale.RECENT) {
                val newRecentData = sensorData?.recent?.toMutableMap()
                newRecentData?.let {
                    it.remove(data.key)
                    val newSensorData =
                        sensorData.copy(recent = newRecentData.toMap())
                    map[data.sensorName] = newSensorData
                }
            }
            if (data.scale == Scale.MINUTE) {
                val newMinuteData = sensorData?.minute?.toMutableMap()
                newMinuteData?.let {
                    it.remove(data.key)
                    val newSensorData =
                        sensorData.copy(recent = newMinuteData.toMap())
                    map[data.sensorName] = newSensorData
                }
            }
            _dataMap.postValue(map)
        }
    }

    private fun handleUpdateData(data: Update) {
        _dataMap.value?.toMutableMap()?.let { map ->
            val sensorData = map[data.sensorName]
            if (data.scale == Scale.RECENT) {
                val newRecentData = sensorData?.recent?.toMutableMap()
                newRecentData?.let {
                    it.put(data.key, data.value)
                    val newSensorData =
                        sensorData.copy(recent = newRecentData.toMap())
                    map[data.sensorName] = newSensorData

                }
            }
            if (data.scale == Scale.MINUTE) {
                val newMinuteData = sensorData?.minute?.toMutableMap()
                newMinuteData?.let {
                    it[data.key] = data.value
                    val newSensorData =
                        sensorData.copy(minute = newMinuteData.toMap())
                    map[data.sensorName] = newSensorData
                }
            }
            _dataMap.postValue(map)
        }
    }

    private fun handleInitData(data: Init) {
        newStreamForSensor?.let {
            data.sensorName = it
            _dataMap.value?.toMutableMap()?.let { map ->
                map[data.sensorName!!] = SensorData(
                    sensorName = data.sensorName!!,
                    recent = data.recent.asMap(),
                    minute = data.minute.asMap(),
                    sensorConfig = sensorConfigMap!!.get(data.sensorName)!!
                )
                _dataMap.postValue(map)
            }
        }
        newStreamForSensor = null
    }


    override fun onCleared() {
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            clearSocketConnectionsUseCase(Unit)
        }
        super.onCleared()
    }

    fun updateScale(isRecent: Boolean) {
        isRecentSelected = isRecent
    }

}