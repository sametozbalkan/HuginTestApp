package com.example.hugintestapp

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import hugin.common.lib.d10.POSMessage
import hugin.common.lib.utils.ViewUtils

abstract class BaseProtocolView<T : POSMessage?> protected constructor(
    context: Context?,
    attrs: AttributeSet? = null
) {

    private var messageSequenceNo = 1
    private lateinit var onClickListener: OnClickListener

    protected abstract fun getProtocolObject(): T

    private fun increaseMessageSequenceNo() {
        messageSequenceNo++
    }

    fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener!!
    }

    fun sendMessage() {
        onClickListener.onSend(getProtocolObject())
        increaseMessageSequenceNo()
    }

    interface OnClickListener {
        fun onSend(posMessage: POSMessage?)
    }
}
