package com.jitendraalekar.sock8.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.jitendraalekar.sock8.databinding.ActivityMainBinding
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

class MainActivity : AppCompatActivity() {

    lateinit var socket: io.socket.client.Socket

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val job =  lifecycleScope.launch(Dispatchers.IO) {
            socket = IO.socket(URI.create("http://interview.optumsoft.com"))
            socket.once(Socket.EVENT_CONNECT){
                    args -> addListeners()
            }
            socket.connect()
        }




        /*  socketManager.socket?.on("update"
          ) { args -> println(args) }

          socketManager.socket?.on("delete"
          ) { args -> println(args) }*/
    }

    private fun addListeners() {

        socket?.on("data",Emitter.Listener {
            runOnUiThread {
                binding.status.text = it[0].toString()
            }
            it.toString()
        })
        socket?.emit("subscribe","temperature0")

    }

    override fun onDestroy() {
        super.onDestroy()

        socket?.disconnect()
        socket?.off()
    }
}