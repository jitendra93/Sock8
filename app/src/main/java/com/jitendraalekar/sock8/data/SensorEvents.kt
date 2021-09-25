package com.jitendraalekar.sock8.data

enum class SensorEvents() {
    DATA,
    CONNECTION;

    override fun toString(): String {
        return this.name.lowercase()
    }

   }