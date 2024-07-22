package com.example.hugintestapp

import hugin.common.lib.d10.POSMessage

interface D10Client {
    fun setListener(listener: D10ResponseListener)
    fun sendD10Message(posMessage: POSMessage)
}