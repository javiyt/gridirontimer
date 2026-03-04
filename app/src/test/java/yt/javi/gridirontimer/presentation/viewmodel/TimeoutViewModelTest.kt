package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeoutViewModelTest {

    @Test
    fun `startTimer creates and starts countdown`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimeoutViewModel(scheduler)

        viewModel.startTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
        assertTrue(scheduler.latest().started)
    }

    @Test
    fun `tick and finish update timeout values`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimeoutViewModel(scheduler)

        viewModel.startTimer()
        scheduler.latest().emitTick(10_000L)
        assertEquals(10_000L, viewModel.time.value)

        scheduler.latest().finish()
        assertEquals(0L, viewModel.time.value)
        assertTrue(viewModel.state.value is TimerState.Finished)
    }

    @Test
    fun `pause and resume update state`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimeoutViewModel(scheduler)
        viewModel.startTimer()
        scheduler.latest().emitTick(40_000L)

        viewModel.pauseTimer()
        assertTrue(viewModel.state.value is TimerState.Paused)
        assertTrue(scheduler.timers[0].canceled)

        viewModel.resumeTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
        assertEquals(2, scheduler.timers.size)
        assertEquals(60_000L, scheduler.latest().remainingMs)
    }

    @Test
    fun `stopTimer from running sets idle`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimeoutViewModel(scheduler)
        viewModel.startTimer()

        viewModel.stopTimer()

        assertTrue(viewModel.state.value is TimerState.Idle)
        assertTrue(scheduler.latest().canceled)
    }
}
