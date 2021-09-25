package com.jitendraalekar.sock8.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.jitendraalekar.sock8.R
import com.jitendraalekar.sock8.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //todo handle no internet

    lateinit var binding: ActivityMainBinding

    val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*      mainViewModel.sensorsState.observe(this@MainActivity) {
                  binding.sensors.adapter = ArrayAdapter(
                      binding.sensors.context,
                      android.R.layout.simple_list_item_1, it.keys.toList()
                  )
                  binding.sensors.onItemClickListener =
                      AdapterView.OnItemClickListener { _, view, position, _
                          ->
                          mainViewModel.subscribeToSensor(it.keys.toList()[position])
                      }
              }*/

        val listOfColors = listOf<Int>(Color.BLACK,
            Color.BLUE,
            Color.CYAN,
            Color.RED,
            Color.YELLOW,
            Color.MAGENTA,
            Color.LTGRAY,
            Color.DKGRAY,
            Color.GREEN)

        mainViewModel.dataMap.observe(this) {
            if (it.isNotEmpty()) {

                val dataSets: MutableList<ILineDataSet> = mutableListOf()
                it.entries.forEachIndexed { index, e ->
                    val firstElement = e.value.recent.entries.elementAt(0)
                    val startSeconds = firstElement.key.epochSecond
                    val entry = e.value.recent.map {

                        println("${it.key.minusSeconds(startSeconds).epochSecond.toFloat()} , ${it.value.toFloat()}")

                        Entry(
                            it.key.minusSeconds(startSeconds).epochSecond.toFloat(),
                            it.value.toFloat()
                        )

                    }.toList()

                    val lineDataSet = LineDataSet(entry, e.key)
                    lineDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    lineDataSet.color = listOfColors[index.mod(listOfColors.size)]
                    dataSets.add(lineDataSet)
                }



                with(binding.chartsView) {
                    setBackgroundColor(Color.WHITE)
                    data = LineData(dataSets)
                    xAxis.setDrawLabels(false)
                    val yAxis = axisLeft
                    yAxis.removeAllLimitLines()
                    yAxis.axisMinimum = 0f
//                    val upperLimitLine = LimitLine(value.sensorConfig.max, "Max safe temperature")
//                    upperLimitLine.lineColor = Color.RED
//                    val lowerLimitLine = LimitLine(value.sensorConfig.min, "Min safe temperature")
//                    lowerLimitLine.lineColor = Color.RED
//                    upperLimitLine.enableDashedLine(10f, 5f, 0f)
//                    lowerLimitLine.enableDashedLine(10f, 5f, 0f)
//                    yAxis.addLimitLine(upperLimitLine)
//                    yAxis.addLimitLine(lowerLimitLine)
                    invalidate()
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                mainViewModel.sensorsState.value?.let {
                    SensorSelectionDialog(it).apply {
                        listener = object : SensorSelectionDialog.NoticeDialogListener {


                            override fun onDialogPositiveClick(listOfSensors: Map<String, Boolean>) {
                                mainViewModel.updated(listOfSensors)
                            }

                        }
                    }.show(
                        supportFragmentManager,
                        null
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}

