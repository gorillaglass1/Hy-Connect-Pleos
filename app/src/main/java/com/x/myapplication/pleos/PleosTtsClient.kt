package com.x.myapplication.pleos

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnServerConnectionListener
import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper

class PleosTtsClient(
    context: Context,
    private val clientId: String,
    private val clientSecret: String,
    private val callback: Callback? = null,
    mode: Mode = Mode.HYBRID,
) {
    interface Callback {
        fun onReady()
        fun onStarted()
        fun onDone()
        fun onError(message: String)
        fun onServerConnected()
        fun onServerFailed(message: String)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val tts = TextToSpeech(
        context.applicationContext,
        if (clientId.isBlank() || clientSecret.isBlank()) Mode.ON_DEVICE else mode
    )
    private var isInitialized = false
    private var pendingText: String? = null

    private val eventListener = object : EventListener {
        override fun onReady() {
            mainHandler.post {
                callback?.onReady()
            }
        }

        override fun onStart() {
            mainHandler.post { callback?.onStarted() }
        }

        override fun onDone() {
            mainHandler.post { callback?.onDone() }
        }

        override fun onError(errMsg: String) {
            mainHandler.post { callback?.onError(errMsg) }
        }

        override fun onStop() = Unit

        override fun onUpdatedRms(rms: Double) = Unit
    }

    fun initialize() {
        tts.initialize()
        isInitialized = true
        tts.addEventListener(eventListener)
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .build()
        )
        registerAppIfConfigured()
        flushPending()
    }

    fun speak(text: String, clearQueue: Boolean = true) {
        if (text.isBlank()) return

        if (!isInitialized) {
            pendingText = text
            return
        }

        tts.speak(text, clearQueue)
    }

    fun stop() {
        pendingText = null
        tts.stop()
    }

    fun release() {
        runCatching { tts.removeEventListener(eventListener) }
        tts.release()
    }

    private fun registerAppIfConfigured() {
        if (clientId.isBlank() || clientSecret.isBlank()) return

        tts.registerApp(
            clientId,
            clientSecret,
            object : OnServerConnectionListener {
                override fun onConnected() {
                    mainHandler.post {
                        callback?.onServerConnected()
                    }
                }

                override fun onFailed(msg: String) {
                    mainHandler.post { callback?.onServerFailed(msg) }
                }
            },
        )
    }

    private fun flushPending() {
        val text = pendingText ?: return
        if (!isInitialized) return
        pendingText = null
        tts.speak(text, true)
    }
}
