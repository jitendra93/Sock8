package com.jitendraalekar.sock8.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type
import java.time.Instant

class InstantDeserializer : JsonDeserializer<Instant> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Instant {
        val expDate = json.asString
        val milliSinceEpoch = expDate.toDouble().toLong().times( 1_000L )
        val instant = Instant.ofEpochMilli( milliSinceEpoch )
        return instant
    }

}