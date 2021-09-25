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
) : Message( "init")

class Update(
    @SerializedName("key")
    val key: Instant,
    @SerializedName("val")
    val value: Double,
    @SerializedName("sensor")
    val sensorName: String,
    @SerializedName("scale")
    val scale: Scale
) : Message("update")

class Delete(
    @SerializedName("key")
    val key: Instant,
    @SerializedName("sensor")
    val sensorName: String,
    @SerializedName("scale")
    val scale: Scale
) : Message("delete"){
    init {
        assert(type == "delete")
    }
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