package com.jitendraalekar.sock8.domain

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jitendraalekar.sock8.Result
import com.jitendraalekar.sock8.data.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class ConnectSensorDataStreamUseCase @Inject constructor(
    private val repository: ISensorRepository,
    coroutineDispatcher: CoroutineDispatcher,
    private val gson: Gson,
) : FlowUseCase<Unit, Message>(coroutineDispatcher) {


    override fun execute(parameters: Unit): Flow<Result<Message>> {
        return repository.connectToDataStream()
            .map { jsonObj ->
                val res = when (jsonObj.getString("type")) {
                    INIT -> {
                        gson.fromJson(jsonObj.toString(), Init::class.java)

                    }
                    UPDATE -> {
                        gson.fromJson(jsonObj.toString(), Update::class.java)
                    }
                    DELETE -> {
                        gson.fromJson(jsonObj.toString(), Delete::class.java)

                    }
                    else -> throw IllegalStateException("Invalid data $jsonObj")
                }
                Result.Success(res)
            }
    }

}


