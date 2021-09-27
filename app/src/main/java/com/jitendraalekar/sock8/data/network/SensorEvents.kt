package com.jitendraalekar.sock8.data.network

enum class SensorEvents() {
    DATA,
    CONNECTION;

    override fun toString(): String {
        return this.name.lowercase()
    }
}