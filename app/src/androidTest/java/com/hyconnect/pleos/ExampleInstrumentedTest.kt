package com.hyconnect.pleos

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun appContextHasExpectedPackageName() {
        val context = ApplicationProvider.getApplicationContext<HyConnectApplication>()

        assertEquals("com.hyconnect.pleos", context.packageName)
    }

    @Test
    fun applicationProvidesRepository() {
        val application = ApplicationProvider.getApplicationContext<HyConnectApplication>()

        assertNotNull(application.repository)
    }
}
