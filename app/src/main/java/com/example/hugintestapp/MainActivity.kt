package com.example.hugintestapp

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.hugintestapp.ui.theme.HuginTestAppTheme
import hugin.common.lib.constants.IntentConsts
import hugin.common.lib.constants.MessengerConsts
import hugin.common.lib.helper.BaseTarget

class MainActivity : ComponentActivity() {
    private lateinit var d10Client: D10Client
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        d10Client = SFAClient(this)
        setContent {
            HuginTestAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(onClick = {
                        getTerminalInfo()
                    }, modifier = Modifier.padding(innerPadding)) {
                        Text(text = "Aygıt Kocaman")
                    }
                }
            }
        }
    }

    private fun getTerminalInfo() {
        val serviceManager: SFAClient? = if (d10Client is SFAClient) {
            d10Client as SFAClient?
        } else {
            return
        }
        serviceManager!!.sendTerminalInfo(object : SFAResponseListener {
            override fun onResponse(msg: Message?) {
                if (MessengerConsts.ACTION_TERMINAL_INFO == msg!!.what) {
                    val data = msg.data
                    data.classLoader = BaseTarget::class.java.classLoader
                    val data2 = data.getString(IntentConsts.EXTRA_DEVICE_MODEL)
                    val data3 = data.getBoolean(IntentConsts.EXTRA_SUPPORT_507)
                    Log.e("DALGA", data2.toString())
                    Log.e("DALAG2", data3.toString())
                } else {
                    Log.e("HATA", "BOZUK")
                }
            }
        })
    }
}