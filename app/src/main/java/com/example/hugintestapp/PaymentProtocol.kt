package com.example.hugintestapp

import hugin.common.lib.d10.POSMessage
import hugin.common.lib.d10.PaymentRequest
import hugin.common.lib.d10.tables.PrintFormatType

abstract class PaymentProtocol<T : POSMessage?> protected constructor() {

    var messageSequenceNo = 1
    private lateinit var onClickListener: OnClickListener

    protected abstract fun getProtocolObject(): T

    private fun increaseMessageSequenceNo() {
        messageSequenceNo++
    }

    fun setOnClickListener(listener: OnClickListener?) {
        if (listener != null) {
            onClickListener = listener
        }
    }

    fun sendMessage() {
        onClickListener.onSend(getProtocolObject())
        increaseMessageSequenceNo()
    }

    interface OnClickListener {
        fun onSend(posMessage: POSMessage?)
    }
}

class PaymentRequestProtocol(private val tranType: Int) : PaymentProtocol<PaymentRequest?>() {
    override fun getProtocolObject(): PaymentRequest {
        return createPaymentRequest(tranType = tranType)
    }

    private fun createPaymentRequest(tranType: Int): PaymentRequest {
        val builder = PaymentRequest.Builder(
            "555555555", messageSequenceNo, 20.0
        ).setTranType(tranType).setPrintFormatType(PrintFormatType.PRINT_ON_DEVICE.ordinal)
        return builder.build()
    }
}
