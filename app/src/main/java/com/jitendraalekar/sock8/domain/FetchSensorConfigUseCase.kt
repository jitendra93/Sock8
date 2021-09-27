package com.jitendraalekar.sock8.domain

import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.ISensorRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.inject.Inject

class FetchSensorConfigUseCase @Inject
constructor(private val repository: ISensorRepository,
            coroutineDispatcher: CoroutineDispatcher)
    : UseCase<Unit, JsonObject>(coroutineDispatcher) {
    override suspend fun execute(parameters: Unit) : JsonObject{
        return repository.getSensorConfig()
    }
}