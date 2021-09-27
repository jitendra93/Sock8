package com.jitendraalekar.sock8.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.lang.Exception
import com.jitendraalekar.sock8.Result
import timber.log.Timber

abstract class UseCase<in P, R> ( val coroutineDispatcher: CoroutineDispatcher){

    suspend  operator fun invoke(parameters : P) : Result<R>{
        return try {
            withContext(coroutineDispatcher){
                execute(parameters).let {
                    Result.Success(it)
                }
            }
        }catch (e : Exception){
            Timber.d(e)
            Result.Error(e)
        }
    }

    /**
     * override this method to perform actual task
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P) : R
}