package com.jitendraalekar.sock8.di

import com.jitendraalekar.sock8.data.ISensorRepository
import com.jitendraalekar.sock8.data.SensorRepositoryImpl
import com.jitendraalekar.sock8.data.network.ISocketManager
import com.jitendraalekar.sock8.data.network.SensorService
import com.jitendraalekar.sock8.data.network.SocketManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

const val BASE_URL : String = "http://interview.optumsoft.com"

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {


    companion object{
        @Singleton
        @Provides
        fun createRetrofitInstance() : Retrofit {
            return Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        fun createSensorService(retrofit: Retrofit) : SensorService {
            return retrofit.create(SensorService::class.java)
        }

    }
    @Singleton
    @Binds
    abstract fun provideSocketManager(socketManager: SocketManager) : ISocketManager

    @Singleton
    @Binds
    abstract fun provideSensorRepository(sensorRepositoryImpl: SensorRepositoryImpl) : ISensorRepository

}