package com.example.hugintestapp

import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.hugintestapp.ui.theme.HuginTestAppTheme
import hugin.common.lib.constants.IntentConsts
import hugin.common.lib.constants.MessengerConsts
import hugin.common.lib.d10.POSMessage
import hugin.common.lib.helper.BaseTarget

class MainActivity : ComponentActivity() {
    private lateinit var d10Client: D10Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        d10Client = SFAClient(this)
        setContent {
            HuginTestAppTheme {
                var isSupport507 by remember { mutableStateOf("") }
                var errorCode by remember { mutableIntStateOf(0) }
                var isDeveloper by remember { mutableStateOf("") }
                var printType by remember { mutableIntStateOf(0) }
                var deviceModel by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Text(text = "Support 507: $isSupport507")
                        Text(text = "Error Code: $errorCode")
                        Text(text = "Developer: $isDeveloper")
                        Text(text = "Print Type: $printType")
                        Text(text = "Device Model: $deviceModel")
                        Button(
                            onClick = {
                                getTerminalInfo(
                                    onResult = { model, developer, support507, printer ->
                                        deviceModel = model
                                        isDeveloper = developer
                                        isSupport507 = support507
                                        printType = printer
                                    },
                                    onError = { code ->
                                        errorCode = code
                                    }
                                )
                            },
                        ) {
                            Text(text = "Verileri Çek")
                        }
                        Button(onClick = {
                            printJson()
                        }) {
                            Text(text = "Yazdır")
                        }
                        Button(onClick = {
                            openTarget()
                        }) {
                            Text(text = "Open Target")
                        }
                        Button(onClick = {
                            val paymentRequestProtocol = PaymentRequestProtocol(this@MainActivity, null)
                            paymentRequestProtocol.setOnClickListener(object : BaseProtocolView.OnClickListener {
                                override fun onSend(posMessage: POSMessage?) {
                                    if (posMessage != null) {
                                        (d10Client as SFAClient).sendD10Message(posMessage)
                                    }
                                    else{
                                        Log.e("mesaj yok", "mesaj yok")
                                    }
                                }
                            })
                            paymentRequestProtocol.sendMessage()
                        }) {
                            Text(text = "Try Payment")
                        }
                    }
                }
            }
        }
    }

    private fun getTerminalInfo(
        onResult: (String, String, String, Int) -> Unit,
        onError: (Int) -> Unit
    ) {
        val serviceManager: SFAClient? = if (d10Client is SFAClient) {
            d10Client as SFAClient?
        } else {
            return
        }

        serviceManager!!.sendTerminalInfo(object : SFAResponseListener {
            override fun onResponse(msg: Message?) {
                if (msg != null && MessengerConsts.ACTION_TERMINAL_INFO == msg.what) {
                    val data = msg.data
                    data.classLoader = BaseTarget::class.java.classLoader
                    val deviceModel = data.getString(IntentConsts.EXTRA_DEVICE_MODEL, "")
                    val isSupport507 =
                        data.getBoolean(IntentConsts.EXTRA_SUPPORT_507, false).toString()
                    val developer =
                        data.getBoolean(IntentConsts.EXTRA_IS_DEVELOPER_DEVICE, false).toString()
                    val printer = data.getInt(IntentConsts.EXTRA_PRINTER_TYPE, 0)
                    onResult(deviceModel, developer, isSupport507, printer)
                } else {
                    onError(msg?.what ?: -1)
                }
            }
        })
    }


    private fun printJson() {
        val serviceManager: SFAClient? = if (d10Client is SFAClient) {
            d10Client as SFAClient?
        } else {
            return
        }

        serviceManager!!.onPrintFreeFormat(strFreeFormat, object : SFAResponseListener {
            override fun onResponse(msg: Message?) {
                if (msg != null && MessengerConsts.ACTION_PRINT_FREE_FORMAT == msg.what) {
                    val data = msg.data
                    data.classLoader = BaseTarget::class.java.classLoader
                } else {
                    Log.e("HATA", "SFA hatası")
                }
            }
        })
    }

    private fun openTarget() {
        val serviceManager: SFAClient? = if (d10Client is SFAClient) {
            d10Client as SFAClient?
        } else {
            return
        }

        serviceManager!!.sendOpenTarget(object : SFAResponseListener {
            override fun onResponse(msg: Message?) {
                if (msg != null && MessengerConsts.ACTION_OPEN_TARGET == msg.what) {
                    val data = msg.data
                    data.classLoader = BaseTarget::class.java.classLoader
                } else {
                    Log.e("HATA", "Open Target hatası")
                }
            }
        })
    }
}

private const val strFreeFormat = """[
  {
    "attr": {
      "align": "center",
      "font": "normal",
      "lineFeed": true,
      "style": "normal"
    },
    "type": "TEXT",
    "value": "HUGIN PAZARLAMA"
  },
  {
    "type": "PAPERSKIP",
    "value": "2"
  },
  {
    "attr": {
      "align": "center",
      "font": "normal",
      "lineFeed": true,
      "style": "bold"
    },
    "type": "TEXT",
    "value": "OTOPARK ARAÇ GİRİŞ FİŞİ"
  },
  {
    "type": "PAPERSKIP",
    "value": "2"
  },
  {
    "attr": {
      "align": "center",
      "font": "normal",
      "lineFeed": true,
      "style": "bold"
    },
    "type": "TEXT",
    "value": "OTOMOBİL"
  },
  {
    "type": "PAPERSKIP",
    "value": "2"
  },
  {
    "attr": {
      "align": "center",
      "font": "normal",
      "lineFeed": false,
      "style": "normal"
    },
    "type": "TEXT",
    "value": "PLAKA"
  },
  {
    "type": "PAPERSKIP",
    "value": "2"
  },
  {
    "attr": {
      "align": "center",
      "height": 150,
      "offset": 0,
      "width": 150
    },
    "type": "QRCODE",
    "value": "Hello"
  },
  {
    "type": "PAPERSKIP",
    "value": "4"
  }
]
"""

