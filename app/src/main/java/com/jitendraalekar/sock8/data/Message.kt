package com.jitendraalekar.sock8.data

import com.google.gson.annotations.SerializedName
import java.time.Instant

sealed class Message(
    val type: String
)

class Init(
    @SerializedName("recent")
    val recent: List<KeyValue>,
    @SerializedName("minute")
    val minute: List<KeyValue>,
    var sensorName: String? = null
) : Message( INIT)

class Update(
    @SerializedName("key")
    val key: Instant,
    @SerializedName("val")
    val value: Double,
    @SerializedName("sensor")
    val sensorName: String,
    @SerializedName("scale")
    val scale: Scale
) : Message(UPDATE)

class Delete(
    @SerializedName("key")
    val key: Instant,
    @SerializedName("sensor")
    val sensorName: String,
    @SerializedName("scale")
    val scale: Scale
) : Message(DELETE){

}


data class KeyValue(val key: Instant, @SerializedName("val") val value: Double)

enum class Scale {
    @SerializedName("recent")
    RECENT,
    @SerializedName("minute")
    MINUTE
}

fun List<KeyValue>.asMap() : Map<Instant,Double>{
    return this.map {
        it.key to it.value
    }.toMap()
}

const val INIT = "init"
const val DELETE = "delete"
const val UPDATE = "update"