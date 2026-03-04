package yt.javi.gridirontimer.presentation

import android.os.Looper
import android.view.KeyEvent
import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun `onCreate sets keep screen on flag`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()

        val flags = activity.window.attributes.flags
        assertTrue(flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
    }

    @Test
    fun `stem primary single press invokes single callback after timeout`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val singlePressCount = AtomicInteger(0)

        setPrivateHandler(activity, "onStemPrimaryPressed") { singlePressCount.incrementAndGet() }

        val consumed = activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        assertTrue(consumed)
        assertEquals(0, singlePressCount.get())

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(360))
        assertEquals(1, singlePressCount.get())
    }

    @Test
    fun `stem primary double press invokes double callback and cancels single callback`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val singlePressCount = AtomicInteger(0)
        val doublePressCount = AtomicInteger(0)

        setPrivateHandler(activity, "onStemPrimaryPressed") { singlePressCount.incrementAndGet() }
        setPrivateHandler(activity, "onStemPrimaryDoublePressed") { doublePressCount.incrementAndGet() }

        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400))

        assertEquals(0, singlePressCount.get())
        assertEquals(1, doublePressCount.get())
    }

    @Test
    fun `stem primary without handlers is consumed`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()

        val consumed = activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )

        assertTrue(consumed)
    }

    @Test
    fun `second press within double window counts as double press`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val singlePressCount = AtomicInteger(0)
        val doublePressCount = AtomicInteger(0)
        setPrivateHandler(activity, "onStemPrimaryPressed") { singlePressCount.incrementAndGet() }
        setPrivateHandler(activity, "onStemPrimaryDoublePressed") { doublePressCount.incrementAndGet() }

        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        setPrivateLong(
            activity,
            "lastStemPrimaryPressAt",
            System.currentTimeMillis() - 200L
        )
        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400))

        assertEquals(0, singlePressCount.get())
        assertEquals(1, doublePressCount.get())
    }

    @Test
    fun `second press after window becomes two single presses`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val singlePressCount = AtomicInteger(0)
        val doublePressCount = AtomicInteger(0)
        setPrivateHandler(activity, "onStemPrimaryPressed") { singlePressCount.incrementAndGet() }
        setPrivateHandler(activity, "onStemPrimaryDoublePressed") { doublePressCount.incrementAndGet() }

        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        ShadowSystemClock.advanceBy(Duration.ofMillis(500))
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        setPrivateLong(activity, "lastStemPrimaryPressAt", 0L)
        activity.onKeyDown(
            KeyEvent.KEYCODE_STEM_PRIMARY,
            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
        )
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400))

        assertEquals(2, singlePressCount.get())
        assertEquals(0, doublePressCount.get())
    }

    @Test
    fun `triple rapid press triggers chained doubles`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val singlePressCount = AtomicInteger(0)
        val doublePressCount = AtomicInteger(0)
        setPrivateHandler(activity, "onStemPrimaryPressed") { singlePressCount.incrementAndGet() }
        setPrivateHandler(activity, "onStemPrimaryDoublePressed") { doublePressCount.incrementAndGet() }

        repeat(3) {
            activity.onKeyDown(
                KeyEvent.KEYCODE_STEM_PRIMARY,
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_STEM_PRIMARY)
            )
            ShadowSystemClock.advanceBy(Duration.ofMillis(100))
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
        }
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))

        assertEquals(0, singlePressCount.get())
        assertEquals(2, doublePressCount.get())
    }

    private fun setPrivateHandler(activity: MainActivity, fieldName: String, callback: () -> Unit) {
        val field = MainActivity::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(activity, callback)
    }

    private fun setPrivateLong(activity: MainActivity, fieldName: String, value: Long) {
        val field = MainActivity::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.setLong(activity, value)
    }
}
