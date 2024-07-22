package com.example.hugintestapp

import hugin.common.lib.d10.POSMessage

interface D10ResponseListener {
    fun onResponse(posMessage: POSMessage?)
    fun onError(errorCode: Int)
}