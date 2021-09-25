package com.jitendraalekar.sock8.domain

import com.jitendraalekar.sock8.data.ISensorRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UnSubscribeSensorUseCase @Inject
constructor(private val repository: ISensorRepository,
            coroutineDispatcher: CoroutineDispatcher)
    : UseCase<String, Unit>(coroutineDispatcher) {
    override suspend fun execute(parameters: String) {
        repository.unSubscribeToSensor(parameters)
    }
}