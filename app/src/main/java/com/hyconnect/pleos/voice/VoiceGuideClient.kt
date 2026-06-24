package com.hyconnect.pleos.voice

import android.content.Context
import android.media.AudioAttributes
import android.util.Log
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import java.util.Locale

/**
 * Gleo AI TextToSpeech 음성 안내 래퍼.
 *
 * 사용 패턴(Vehicle/Navi 래퍼와 동일):
 *   Activity.onCreate  → [initialize]
 *   Activity.onDestroy → [release]
 *
 * 정책: 서버 인증 없이 단말에서만 합성하는 **ON_DEVICE** 모드만 사용한다.
 *       ON_DEVICE 모드는 registerApp(clientID/secret) 호출이 필요 없다.
 * 매니페스트에 `pleos.car.permission.TTS_SERVICE`와 TTS_SERVICE 질의(queries)가 선언돼 있어야 한다.
 *
 * Pleos 차량 음성 서비스가 없는 일반 에뮬레이터/기기에서는 SDK 호출이 실패할 수 있으므로
 * 모든 접근을 예외로 감싸 앱이 죽지 않고 조용히 무시(no-op)되게 한다.
 */
class VoiceGuideClient(context: Context) {

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)

    // initialize() 호출 여부. ON_DEVICE 모드에서는 onReady 이벤트가 별도로 오지 않으므로
    // (서버 연결 readiness용으로 보임) initialize 직후부터 speak가 가능하다고 본다.
    @Volatile
    private var initialized = false

    /** 한 멘트의 재생이 끝났을 때 호출한다(예: 안내 후 마이크 청취 시작). */
    var onUtteranceDone: (() -> Unit)? = null

    private val eventListener = object : EventListener {
        override fun onReady() { Log.d(LOG_TAG, "TTS onReady") }
        override fun onStart() { Log.d(LOG_TAG, "TTS onStart") }
        override fun onDone() {
            Log.d(LOG_TAG, "TTS onDone")
            onUtteranceDone?.invoke()
        }
        override fun onStop() { Log.d(LOG_TAG, "TTS onStop") }
        override fun onUpdatedRms(rms: Double) { /* 음량 레벨(미사용) */ }
        override fun onError(errMsg: String) { Log.w(LOG_TAG, "TTS onError: $errMsg") }
    }

    fun initialize() {
        runCatching {
            Log.d(LOG_TAG, "TTS initialize (ON_DEVICE)")
            // SDK 내부 서비스가 initialize()에서 준비되므로, 다른 설정/리스너보다 먼저 호출해야 한다.
            // (initialize 전 addEventListener 호출 시 lateinit 미초기화로 예외 발생)
            tts.initialize()
            tts.addEventListener(eventListener)
            // 차내 안내 음성으로 분류해 다른 미디어와의 오디오 포커스를 적절히 다룬다.
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .build(),
            )
            tts.setLocale(Locale.KOREAN)
            initialized = true
        }.onFailure { Log.w(LOG_TAG, "TTS initialize 실패 — 음성 안내 비활성.", it) }
    }

    /** 멘트를 음성으로 출력한다. initialize() 전이면 무시한다. */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (!initialized) {
            Log.d(LOG_TAG, "TTS not initialized — skip: $text")
            return
        }
        speakNow(text)
    }

    private fun speakNow(text: String) {
        runCatching {
            // 두 번째 인자(flush=true): 진행 중인 안내를 끊고 새 멘트를 즉시 재생한다.
            tts.speak(text, true)
        }.onFailure { Log.w(LOG_TAG, "TTS speak 실패.", it) }
    }

    fun stop() {
        runCatching { tts.stop() }.onFailure { Log.w(LOG_TAG, "TTS stop 실패.", it) }
    }

    fun release() {
        runCatching {
            Log.d(LOG_TAG, "TTS release")
            tts.removeEventListener(eventListener)
            tts.release()
        }.onFailure { Log.w(LOG_TAG, "TTS release 실패.", it) }
        initialized = false
    }

    private companion object {
        const val LOG_TAG = "HyConnect.Tts"
    }
}
