package com.jitendraalekar.sock8.domain

import com.jitendraalekar.sock8.data.ISensorRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class FetchSensorNamesUseCase @Inject
constructor(private val repository: ISensorRepository,
            coroutineDispatcher: CoroutineDispatcher)
    : UseCase<Unit, List<String>>(coroutineDispatcher) {
    override suspend fun execute(parameters: Unit) : List<String>{
        return repository.getSensorNames()
    }
}