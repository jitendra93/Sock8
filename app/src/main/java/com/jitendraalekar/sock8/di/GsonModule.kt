package com.jitendraalekar.sock8.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jitendraalekar.sock8.data.InstantDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant

@Module
@InstallIn(SingletonComponent::class)
object GsonModule {

    @Provides
    fun provideGson() : Gson{
        return GsonBuilder().registerTypeAdapter(
            Instant::class.java,
            InstantDeserializer()
        ).create()
    }
}