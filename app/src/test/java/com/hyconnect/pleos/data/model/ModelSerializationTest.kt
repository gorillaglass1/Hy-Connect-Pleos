package com.hyconnect.pleos.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelSerializationTest {
    private val gson = Gson()

    @Test
    fun vehicleStateDeserializesSnakeCaseFields() {
        val state = gson.fromJson(
            """
            {
              "hydrogen_percent": 63,
              "vehicle_range_km": 382,
              "message": "Ready"
            }
            """.trimIndent(),
            VehicleState::class.java,
        )

        assertEquals(63, state.hydrogenPercent)
        assertEquals(382, state.vehicleRangeKm)
        assertEquals("Ready", state.message)
    }

    @Test
    fun hydrogenStationDeserializesOptionalCoordinatesAndRecommendationFlag() {
        val station = gson.fromJson(
            """
            {
              "id": "station-1",
              "name": "HyConnect Gangnam",
              "address": "Seoul",
              "status": "OPEN",
              "pressure_info": "700bar",
              "distance_km": 2.4,
              "wait_minutes": 7,
              "is_recommended": true,
              "latitude": 37.4979,
              "longitude": 127.0276
            }
            """.trimIndent(),
            HydrogenStation::class.java,
        )

        assertEquals("station-1", station.id)
        assertEquals("HyConnect Gangnam", station.name)
        assertEquals("700bar", station.pressureInfo)
        assertEquals(2.4, station.distanceKm, 0.0)
        assertEquals(7, station.waitMinutes)
        assertEquals(true, station.isRecommended)
        assertEquals(37.4979, station.latitude ?: 0.0, 0.0)
        assertEquals(127.0276, station.longitude ?: 0.0, 0.0)
    }
}
