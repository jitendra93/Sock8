package com.jitendraalekar.sock8.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.jitendraalekar.sock8.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //todo handle no internet

    lateinit var binding: ActivityMainBinding

    val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.sensorsState.observe(this@MainActivity) {
            binding.sensors.adapter = ArrayAdapter(
                binding.sensors.context,
                android.R.layout.simple_list_item_1, it.keys.toList()
            )
            binding.sensors.onItemClickListener =
                AdapterView.OnItemClickListener { _, view, position, _
                    ->
                    mainViewModel.loadSensorData(it.keys.toList()[position])
                }
        }

        mainViewModel.dataMap.observe(this) {

            binding.status.text = it.entries.map { it.value.recent.size }.toList().joinToString("\n")
        }
    }
}

