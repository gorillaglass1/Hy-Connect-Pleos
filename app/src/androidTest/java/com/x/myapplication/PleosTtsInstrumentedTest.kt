package com.x.myapplication

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.x.myapplication.pleos.PleosTtsClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PleosTtsInstrumentedTest {
    private var ttsClient: PleosTtsClient? = null

    @After
    fun tearDown() {
        ttsClient?.release()
        ttsClient = null
    }

    @Test
    fun initializeAndSpeakGreeting() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val playbackLatch = CountDownLatch(1)
        val events = mutableListOf<String>()

        ttsClient = PleosTtsClient(
            context = context,
            clientId = BuildConfig.PLEOS_CLIENT_ID,
            clientSecret = BuildConfig.PLEOS_CLIENT_SECRET,
            callback = object : PleosTtsClient.Callback {
                override fun onReady() {
                    events += "ready"
                    Log.i(TAG, "TTS onReady")
                }

                override fun onStarted() {
                    events += "started"
                    Log.i(TAG, "TTS onStarted")
                    playbackLatch.countDown()
                }

                override fun onDone() {
                    events += "done"
                    Log.i(TAG, "TTS onDone")
                    playbackLatch.countDown()
                }

                override fun onError(message: String) {
                    events += "error:$message"
                    Log.e(TAG, "TTS onError: $message")
                    playbackLatch.countDown()
                }

                override fun onServerConnected() {
                    events += "serverConnected"
                    Log.i(TAG, "TTS onServerConnected")
                }

                override fun onServerFailed(message: String) {
                    events += "serverFailed:$message"
                    Log.e(TAG, "TTS onServerFailed: $message")
                    playbackLatch.countDown()
                }
            },
        ).also { it.initialize() }

        ttsClient?.speak("안녕하세요? 저는 Gleo AI 테스트예요.")

        val playbackOrErrorResponded = playbackLatch.await(10, TimeUnit.SECONDS)

        Log.i(TAG, "TTS test events: ${events.joinToString()}")

        assertTrue(
            "TTS did not start playback or report error within 10 seconds. Events: $events",
            playbackOrErrorResponded,
        )
        assertTrue(
            "TTS reported an error. Events: $events",
            events.none { it.startsWith("error:") || it.startsWith("serverFailed:") },
        )
    }

    private companion object {
        const val TAG = "PleosTtsTest"
    }
}
