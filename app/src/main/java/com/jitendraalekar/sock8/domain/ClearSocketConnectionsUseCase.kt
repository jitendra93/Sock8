package com.jitendraalekar.sock8.domain

import com.google.gson.JsonObject
import com.jitendraalekar.sock8.data.ISensorRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.inject.Inject

class ClearSocketConnectionsUseCase @Inject
constructor(
    private val repository: ISensorRepository,
    coroutineDispatcher: CoroutineDispatcher
) : UseCase<Unit, Unit>(coroutineDispatcher) {
    override suspend fun execute(parameters: Unit) {
        repository.disconnect()
        repository.removeAllListeners()

    }
}