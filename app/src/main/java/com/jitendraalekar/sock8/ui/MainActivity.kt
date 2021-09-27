package com.jitendraalekar.sock8.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.jitendraalekar.sock8.R
import com.jitendraalekar.sock8.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.util.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jitendraalekar.sock8.data.SensorData


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    lateinit var binding: ActivityMainBinding

    val mainViewModel by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        mainViewModel.dataMap.observe(this) {
            if (it.isNotEmpty()) {

                val min = setScale(it)
                binding.chartsView.axisLeft.removeAllLimitLines()

                val dataSets: MutableList<ILineDataSet> = mutableListOf()
                it.entries.forEachIndexed { index, e ->

                    val scaledEntry = getScaledEntries(e)

                    val entry = scaledEntry.map {
                        Entry(
                            it.key.epochSecond.minus(min.epochSecond).toFloat(),
                            it.value.toFloat(),
                        )
                    }.toList()

                    val lineDataSet = LineDataSet(entry, e.key)
                    lineDataSet.color = listOfColors[index.mod(listOfColors.size)]
                    dataSets.add(lineDataSet)
                    with(binding.chartsView) {
                        data = LineData(dataSets)
                        val upperLimitLine =
                            LimitLine(e.value.sensorConfig.max, "Max safe ${e.key}")
                        upperLimitLine.lineColor = Color.RED
                        val lowerLimitLine =
                            LimitLine(e.value.sensorConfig.min, "Min safe ${e.key}")
                        lowerLimitLine.lineColor = Color.RED
                        upperLimitLine.enableDashedLine(10f, 5f, 0f)
                        lowerLimitLine.enableDashedLine(10f, 5f, 0f)
                        axisLeft.addLimitLine(upperLimitLine)
                        axisLeft.addLimitLine(lowerLimitLine)
                    }
                }
                with(binding.chartsView) {
                    setBackgroundColor(Color.WHITE)
                    data = LineData(dataSets)
                    axisLeft.axisMinimum = 0f
                    axisRight.axisMinimum = 0f
                    xAxis.setDrawLabels(false)
                    axisRight.setDrawGridLines(false)
                    invalidate()
                }
            } else {
                binding.chartsView.apply {
                    clear()
                }
            }
        }
    }

    private fun setScale(it: Map<String, SensorData>): Instant {
        var min = Instant.MAX
        it.entries.forEach { e ->
            val scaledEntry = getScaledEntries(e)
            scaledEntry.forEach {
                if (it.key < min) {
                    min = it.key
                }
            }
        }
        return min
    }

    private fun getScaledEntries(e: Map.Entry<String, SensorData>) =
        if (mainViewModel.isRecentSelected) {
            e.value.recent
        } else {
            e.value.minute
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.remove -> {
                mainViewModel.sensorsState.value?.filter { it.value }?.let {
                    SensorSelectionDialog(it.keys.toList()).apply {
                        listener = object : SensorSelectionDialog.NoticeDialogListener {
                            override fun onDialogPositiveClick(sensor: String) {
                                mainViewModel.requestSensorData(sensor, false)
                            }
                        }
                    }.show(
                        supportFragmentManager,
                        null
                    )
                }
                true
            }
            R.id.add -> {
                mainViewModel.sensorsState.value?.filter { !it.value }?.let {
                    SensorSelectionDialog(it.keys.toList()).apply {
                        listener = object : SensorSelectionDialog.NoticeDialogListener {
                            override fun onDialogPositiveClick(sensor: String) {
                                mainViewModel.requestSensorData(sensor, true)
                            }
                        }
                    }.show(
                        supportFragmentManager,
                        null
                    )
                }
                true
            }
            R.id.filter -> {
                ScaleSelectionDialog(mainViewModel.isRecentSelected).apply {
                    listener = object : ScaleSelectionDialog.NoticeDialogListener {
                        override fun onDialogPositiveClick(isRecent: Boolean) {
                            mainViewModel.updateScale(isRecent)
                        }
                    }
                }.show(
                    supportFragmentManager,
                    null
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    val listOfColors = listOf<Int>(
        Color.BLACK,
        Color.BLUE,
        Color.CYAN,
        Color.RED,
        Color.YELLOW,
        Color.MAGENTA,
        Color.LTGRAY,
        Color.DKGRAY,
        Color.GREEN
    )
}

