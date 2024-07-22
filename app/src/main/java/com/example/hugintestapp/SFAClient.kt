package com.example.hugintestapp

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import hugin.common.lib.constants.IntentConsts
import hugin.common.lib.constants.MessengerConsts
import hugin.common.lib.d10.*
import hugin.common.lib.log.LogH

class SFAClient(private var activity: Activity?) : D10Client {
    private lateinit var clientMessenger: Messenger
    private var bound = false
    private var serviceMessenger: Messenger? = null
    private var fiscalConn: ServiceConnection? = null
    private var runOnConnect: Runnable? = null
    override fun setListener(listener: D10ResponseListener) {
        clientMessenger = Messenger(IncomingHandler(listener))
    }

    override fun sendD10Message(posMessage: POSMessage) {
        val runnable = Runnable {
            val msg = Message.obtain(null, MessengerConsts.ACTION_D10_MESSAGE, 0, 0)
            val data = Bundle()
            data.putByteArray("D10", posMessage.message)
            msg.data = data
            try {
                msg.replyTo = clientMessenger
                serviceMessenger!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        if (bound) {

            // Create and send a message to the service, using a supported 'what' value
            runnable.run()
        } else {
            bindService(runnable)
        }
    }

    fun sendTerminalInfo(incomingHandler: SFAResponseListener) {
        val runnable = Runnable {
            val msg = Message.obtain(null, MessengerConsts.ACTION_TERMINAL_INFO, 0, 0)
            val data = Bundle()
            msg.data = data
            try {
                msg.replyTo = Messenger(object : Handler() {
                    override fun handleMessage(msg: Message) {
                        incomingHandler.onResponse(msg)
                    }
                })
                serviceMessenger!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        if (bound) {
            Log.e("Helalkeee", "Bravo")
            // Create and send a message to the service, using a supported 'what' value
            runnable.run()
        } else {
            bindService(runnable)
        }
    }

    fun sendOpenTarget(incomingHandler: SFAResponseListener) {
        val runnable = Runnable {
            val msg = Message.obtain(null, MessengerConsts.ACTION_OPEN_TARGET, 0, 0)
            val data = Bundle()
            data.putString(IntentConsts.EXTRA_TARGET_ID, IntentConsts.NL_TECHPOS_PACKAGE)
            msg.data = data
            try {
                msg.replyTo = Messenger(object : Handler() {
                    override fun handleMessage(msg: Message) {
                        incomingHandler.onResponse(msg)
                    }
                })
                serviceMessenger!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        if (bound) {
            // Create and send a message to the service, using a supported 'what' value
            runnable.run()
        } else {
            bindService(runnable)
        }
    }

    private fun bindService(runnable: Runnable?) {
        if (activity != null) {
            val intent = Intent()
                .setComponent(
                    ComponentName(
                        IntentConsts.NL_FISCAL_PACKAGE,
                        IntentConsts.NL_FISCAL_SERVICE
                    )
                )
                .putExtra(IntentConsts.ORIGIN_TAG, IntentConsts.ORIGIN_HUGIN_SDK)
            activity!!.bindService(intent, fiscalConn!!, Context.BIND_AUTO_CREATE)
            runOnConnect = runnable
        }
    }

    fun unbindService() {
        if (bound) {
            activity!!.unbindService(fiscalConn!!)
            activity = null
        }
    }

    private class IncomingHandler(var listener: D10ResponseListener) : Handler() {
        override fun handleMessage(msg: Message) {
            val data = msg.data
            when (msg.what) {
                MessengerConsts.ACTION_D10_MESSAGE -> {
                    val responseMes = data.getByteArray(IntentConsts.EXTRA_D10_MESSAGE)
                    if (responseMes != null) {
                        if (responseMes.size < MessageConstants.MIN_PACKET_LEN) {
                            val error: POSMessage = object : POSMessage("", 0, 0) {
                                override fun getMessage(): ByteArray {
                                    return responseMes
                                }
                            }
                            listener.onResponse(error)
                            return
                        }
                        val messageType = findD10MessageType(responseMes)
                        val parsedResponse: POSMessage
                        when (messageType) {
                            MessageTypes.RESP_ENDOFDAY -> {
                                parsedResponse = EndOfDayResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_CONFIG -> {
                                parsedResponse = ConfigurationResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_PAYMENT -> {
                                parsedResponse = PaymentResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_SLIP_COPY -> {
                                val parsedSlipCopyResp =
                                    SlipCopyResponse.Builder(responseMes).build()
                                listener.onResponse(parsedSlipCopyResp)
                            }
                            MessageTypes.RESP_PRINT -> {
                                val printResponse = PrintResponse.Builder(responseMes).build()
                                if (printResponse.slipContent != null) {
                                    listener.onResponse(printResponse)
                                }
                            }
                            MessageTypes.RESP_BANK_LIST -> {
                                val bankListResponse = BankListResponse.Builder(responseMes).build()
                                listener.onResponse(bankListResponse)
                            }
                            MessageTypes.RESP_INFO_QUERY -> {
                                parsedResponse = InfoQueryResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_MAINTENANCE -> {
                                parsedResponse = MaintenanceResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_DEVICE_INFO -> {
                                parsedResponse = DeviceInfoResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            MessageTypes.RESP_TRAN_QUERY -> {
                                parsedResponse = TranQueryResponse.Builder(responseMes).build()
                                listener.onResponse(parsedResponse)
                            }
                            else ->{listener.onResponse(null)}
                        }
                    } else {
                        val errorCode = data.getInt(IntentConsts.EXTRA_ERROR_CODE)
                        listener.onError(errorCode)
                    }
                }
            }
        }

        private fun findD10MessageType(bytes: ByteArray): Int {
            var index = 3
            index += MessageConstants.LEN_SERIAL
            return MessageBuilder.byteArrayToHex(bytes, index, 3)
        }
    }

    init {
        fiscalConn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                LogH.debug("FiscalConn Service Connected")
                serviceMessenger = Messenger(service)
                bound = true
                if (runOnConnect != null) {
                    runOnConnect!!.run()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                LogH.debug("FiscalConn Service Disconnected")
                serviceMessenger = null
                bound = false
            }
        }
        bindService(null)
    }
}