package yt.javi.gridirontimer.presentation.viewmodel

import android.os.Looper
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class TimerViewModelTest {

    @Test
    fun `startTimer moves to running then finishes`() {
        val viewModel = TimerViewModel()

        viewModel.startTimer(2_000L)
        assertTrue(viewModel.state.value is TimerState.Running)

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(2_100))
        assertTrue(viewModel.state.value is TimerState.Finished)
    }

    @Test
    fun `pauseTimer sets paused and resumeTimer sets running`() {
        val viewModel = TimerViewModel()
        viewModel.startTimer(5_000L)

        viewModel.pauseTimer()
        assertTrue(viewModel.state.value is TimerState.Paused)

        viewModel.resumeTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
    }
}
