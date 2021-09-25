package com.jitendraalekar.sock8.data.network

import com.google.gson.JsonObject
import retrofit2.http.GET

interface SensorService {


    @GET("sensornames")
     suspend fun getSensorNames() : List<String>

    @GET("config")
     suspend fun getSensorConfig() : JsonObject
}