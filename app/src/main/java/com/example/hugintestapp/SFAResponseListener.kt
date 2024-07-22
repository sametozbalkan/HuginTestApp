package com.example.hugintestapp

import android.os.Message

interface SFAResponseListener {
    fun onResponse(msg: Message?)
}