package com.hyconnect.pleos.voice

import android.content.Context
import android.util.Log
import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.ResultListener

/**
 * Gleo AI SpeechToText 음성 입력(마이크) 래퍼.
 *
 * "사용자가 마이크로 입력을 받을 수 있는가?" → 가능하다.
 * Pleos 차량 음성 서비스가 마이크 캡처를 담당하므로, 앱은 [startListening]([SpeechToText.request])으로
 * 인식을 시작하고 [ResultListener.onUpdated]로 인식 텍스트를 받는다(별도 RECORD_AUDIO 직접 캡처 불필요).
 *
 * 정책: 서버 인증 없이 단말에서만 인식하는 **ON_DEVICE** 모드만 사용한다.
 *       ON_DEVICE 모드는 registerApp(clientID/secret) 호출이 필요 없다.
 * 매니페스트에 `pleos.car.permission.STT_SERVICE`와 SPEECH_START 질의(queries)가 선언돼 있어야 한다.
 *
 * Pleos 음성 서비스가 없는 일반 에뮬레이터/기기에서는 SDK 호출이 실패할 수 있으므로
 * 모든 접근을 예외로 감싸 앱이 죽지 않게 한다.
 */
class VoiceInputClient(context: Context) {

    private val stt: SpeechToText = SpeechToText(context.applicationContext, Mode.ON_DEVICE)

    /** 인식이 끝난(completed=true) 최종 문장을 전달한다. 부분 인식 결과는 무시한다. */
    var onFinalText: ((String) -> Unit)? = null

    /** 마이크 청취 상태가 바뀌면 호출한다(UI 표시·버튼 상태 등에 사용). */
    var onListeningChanged: ((listening: Boolean) -> Unit)? = null

    /** 인식 오류 시 호출한다. */
    var onError: (() -> Unit)? = null

    @Volatile
    var isListening: Boolean = false
        private set

    private val resultListener = object : ResultListener {
        override fun onReady() { Log.d(LOG_TAG, "STT onReady") }

        override fun onStartedRecognition() {
            Log.d(LOG_TAG, "STT onStartedRecognition")
            isListening = true
            onListeningChanged?.invoke(true)
        }

        override fun onUpdated(stt: String, completed: Boolean) {
            Log.d(LOG_TAG, "STT onUpdated completed=$completed text=$stt")
            if (completed && stt.isNotBlank()) {
                onFinalText?.invoke(stt.trim())
            }
        }

        override fun onUpdatedEpdData(on: Long, off: Long) { /* 발화 구간(EPD) 타이밍(미사용) */ }

        override fun onEndedRecognition() {
            Log.d(LOG_TAG, "STT onEndedRecognition")
            isListening = false
            onListeningChanged?.invoke(false)
        }

        override fun onError() {
            Log.w(LOG_TAG, "STT onError")
            isListening = false
            onListeningChanged?.invoke(false)
            onError?.invoke()
        }
    }

    fun initialize() {
        runCatching {
            Log.d(LOG_TAG, "STT initialize (ON_DEVICE)")
            // setLocale()은 시스템(privileged) 앱 전용 API라 일반 앱에서 호출하면 SecurityException.
            // 일반 앱은 권한(STT_SERVICE)만으로 initialize/addListener/request/stop을 쓸 수 있다.
            stt.initialize()
            stt.addListener(resultListener)
        }.onFailure { Log.w(LOG_TAG, "STT initialize 실패 — 음성 입력 비활성.", it) }
    }

    /** 마이크 청취를 시작한다. 인식 결과는 [onFinalText]로 전달된다. */
    fun startListening() {
        runCatching {
            Log.d(LOG_TAG, "STT request (start listening)")
            stt.request()
        }.onFailure {
            Log.w(LOG_TAG, "STT request 실패.", it)
            onError?.invoke()
        }
    }

    /** 마이크 청취를 중단한다. */
    fun stopListening() {
        runCatching { stt.stop() }.onFailure { Log.w(LOG_TAG, "STT stop 실패.", it) }
    }

    fun release() {
        runCatching {
            Log.d(LOG_TAG, "STT release")
            stt.removeListener(resultListener)
            stt.release()
        }.onFailure { Log.w(LOG_TAG, "STT release 실패.", it) }
        isListening = false
    }

    private companion object {
        const val LOG_TAG = "HyConnect.Stt"
    }
}
