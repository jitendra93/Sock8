package com.jitendraalekar.sock8.util

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import java.time.Instant

object Formatter : LargeValueFormatter() {
    override fun getPointLabel(entry: Entry?): String {
         return Instant.ofEpochSecond(entry!!.x.toLong()).toString() + entry.y.toString()
    }
}