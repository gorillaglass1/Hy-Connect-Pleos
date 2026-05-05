package com.x.myapplication.pleos

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnServerConnectionListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import android.os.Handler
import android.os.Looper

class PleosSttClient(
    context: Context,
    private val clientId: String,
    private val clientSecret: String,
    private val callback: Callback,
    mode: Mode = Mode.HYBRID,
) {
    interface Callback {
        fun onStatusChanged(message: String)
        fun onTextUpdated(text: String, completed: Boolean)
        fun onError()
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val speechToText = SpeechToText(
        context.applicationContext,
        if (clientId.isBlank() || clientSecret.isBlank()) Mode.ON_DEVICE else mode
    )

    private val resultListener = object : ResultListener {
        override fun onUpdated(stt: String, completed: Boolean) {
            post { callback.onTextUpdated(stt, completed) }
        }

        override fun onUpdatedEpdData(on: Long, off: Long) = Unit

        override fun onStartedRecognition() {
            post { callback.onStatusChanged("음성 인식 중입니다.") }
        }

        override fun onEndedRecognition() {
            post { callback.onStatusChanged("음성 인식이 종료되었습니다.") }
        }

        override fun onError() {
            post { callback.onError() }
        }

        override fun onReady() {
            post { callback.onStatusChanged("말씀해주세요.") }
        }
    }

    fun initialize() {
        speechToText.initialize()
        speechToText.addListener(resultListener)
        registerAppIfConfigured()
    }

    fun startRecognition() {
        speechToText.request()
    }

    fun stopRecognition() {
        speechToText.stop()
    }

    fun release() {
        runCatching { speechToText.removeListener(resultListener) }
        speechToText.release()
    }

    private fun registerAppIfConfigured() {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            post { callback.onStatusChanged("Gleo AI client key가 없어 ON_DEVICE/HYBRID 기본 모드로 동작합니다.") }
            return
        }

        speechToText.registerApp(
            clientId,
            clientSecret,
            object : OnServerConnectionListener {
                override fun onConnected() {
                    post { callback.onStatusChanged("Speech 서버에 연결되었습니다.") }
                }

                override fun onFailed(msg: String) {
                    post { callback.onStatusChanged("Speech 서버 등록 실패: $msg") }
                }
            },
        )
    }

    private fun post(action: () -> Unit) {
        mainHandler.post(action)
    }
}
