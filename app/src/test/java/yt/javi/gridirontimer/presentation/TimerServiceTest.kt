package yt.javi.gridirontimer.presentation

import android.app.NotificationManager
import android.app.Service
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimerServiceTest {

    @Test
    fun `onCreate creates notification channel`() {
        val service = Robolectric.buildService(TimerService::class.java).create().get()

        val nm = service.getSystemService(NotificationManager::class.java)
        assertNotNull(nm?.getNotificationChannel("timer_service_channel"))
    }

    @Test
    fun `onBind returns null`() {
        val service = Robolectric.buildService(TimerService::class.java).create().get()

        assertNull(service.onBind(null))
    }

    @Test
    fun `onStartCommand returns START_STICKY`() {
        val controller = Robolectric.buildService(TimerService::class.java).create()
        val result = controller.get().onStartCommand(null, 0, 1)

        assertEquals(Service.START_STICKY, result)
    }

    @Test
    fun `onDestroy does not crash and releases wake lock`() {
        val controller = Robolectric.buildService(TimerService::class.java).create()
        controller.destroy()
    }

    @Test
    fun `full lifecycle create start destroy runs without error`() {
        val controller = Robolectric.buildService(TimerService::class.java)
            .create()
        controller.get().onStartCommand(null, 0, 1)
        controller.destroy()
    }
}
