package com.jitendraalekar.sock8.data

import com.google.gson.annotations.SerializedName
import java.time.Instant


data class SensorData(
    val sensorName : String,
    val recent: Map<Instant, Double>,
    val minute: Map<Instant, Double>,
    val sensorConfig : SensorConfig,
)

data class SensorConfig(val min : Float, val max :Float)