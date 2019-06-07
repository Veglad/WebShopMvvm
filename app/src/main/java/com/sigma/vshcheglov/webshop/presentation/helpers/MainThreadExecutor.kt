package com.sigma.vshcheglov.webshop.presentation.helpres

import android.os.Looper
import com.badoo.mobile.util.WeakHandler
import java.util.concurrent.Executor

class MainThreadExecutor : Executor {
    private val handler = WeakHandler(Looper.getMainLooper())

    override fun execute(r: Runnable) {
        handler.post(r)
    }
}