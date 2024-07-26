package com.example.hugintestapp

import hugin.common.lib.d10.POSMessage
import hugin.common.lib.d10.PaymentRequest
import hugin.common.lib.d10.tables.TransactionType

abstract class PaymentProtocol<T : POSMessage?> protected constructor() {

    private var messageSequenceNo = 1
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

class PaymentRequestProtocolView : PaymentProtocol<PaymentRequest?>() {
    override fun getProtocolObject(): PaymentRequest {
        var paymentRequest: PaymentRequest? = null
        paymentRequest = createPaymentRequest()
        return paymentRequest
    }

    private fun createPaymentRequest(): PaymentRequest {
        val builder = PaymentRequest.Builder(
            "N7A705244708", 1, 20.0
        ).setTranType(TransactionType.SALE)
        return builder.build()
    }
}
