package com.example.hugintestapp

import android.content.Context
import android.util.AttributeSet
import hugin.common.lib.d10.PaymentRequest
import hugin.common.lib.d10.tables.PrintFormatType
import hugin.common.lib.d10.tables.TransactionType

class PaymentRequestProtocol(context: Context, attrs: AttributeSet?) :
    BaseProtocolView<PaymentRequest?>(context, attrs) {
    override fun getProtocolObject(): PaymentRequest {
        var paymentRequest: PaymentRequest? = null
        paymentRequest = createPaymentRequest()
        return paymentRequest
    }
    private fun createPaymentRequest(): PaymentRequest {
        val builder: PaymentRequest.Builder =
            PaymentRequest.Builder(
                "N7A705244708", 1, 20.0
            ).setTranType(TransactionType.SALE)
                .setPrintFormatType(PrintFormatType.PRINT_ON_DEVICE.ordinal)
        return builder.build()
    }
}