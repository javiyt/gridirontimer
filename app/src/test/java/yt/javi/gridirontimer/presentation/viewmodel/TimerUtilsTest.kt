package yt.javi.gridirontimer.presentation.viewmodel

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class TimerUtilsTest {

    @Test
    fun `formatTime returns mm ss`() {
        assertEquals("02:05", TimerUtils.formatTime(125_000L))
    }

    @Test
    fun `formatSeconds returns ss`() {
        assertEquals("09", TimerUtils.formatSeconds(9_000L))
        assertEquals("59", TimerUtils.formatSeconds(59_000L))
    }

    @Test
    fun `vibrate supports one two and three pulses`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        TimerUtils.vibrate(context, pulses = 1)
        TimerUtils.vibrate(context, pulses = 2)
        TimerUtils.vibrate(context, pulses = 3)
    }

    @Test
    @Config(sdk = [30])
    fun `vibrate works on pre S devices`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TimerUtils.vibrate(context, pulses = 2)
    }

    @Test
    @Config(sdk = [34])
    fun `vibrate works on S and above devices`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TimerUtils.vibrate(context, pulses = 2)
    }

    @Test
    fun `playSound does not crash when notification volume is zero`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)

        TimerUtils.playSound(context)
    }

    @Test
    fun `playSound does not crash when notification volume is above zero`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0)

        TimerUtils.playSound(context)
    }
}
