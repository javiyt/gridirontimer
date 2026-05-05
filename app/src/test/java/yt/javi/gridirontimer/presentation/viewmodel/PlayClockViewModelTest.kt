package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayClockViewModelTest {

    @Test
    fun `startTimer creates running countdown`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)

        viewModel.startTimer(2_000L)
        assertTrue(viewModel.state.value is TimerState.Running)
        assertTrue(scheduler.latest().started)
    }

    @Test
    fun `onTick and onFinish update remaining and state`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)

        viewModel.startTimer(2_000L)
        scheduler.latest().emitTick(1_000L)
        assertEquals(1_000L, viewModel.time.value)
        assertTrue(viewModel.state.value is TimerState.Running)

        scheduler.latest().finish()
        assertEquals(0L, viewModel.time.value)
        assertTrue(viewModel.state.value is TimerState.Finished)
    }

    @Test
    fun `pause and resume keep timer active`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)
        viewModel.startTimer(5_000L)
        scheduler.latest().emitTick(4_000L)

        viewModel.pauseTimer()
        assertTrue(viewModel.state.value is TimerState.Paused)
        assertTrue(scheduler.timers[0].canceled)

        viewModel.resumeTimer()
        assertTrue(viewModel.state.value is TimerState.Running)
        assertEquals(2, scheduler.timers.size)
        assertEquals(4_000L, scheduler.latest().remainingMs)
    }

    @Test
    fun `cancelTimer sets idle`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)
        viewModel.startTimer(5_000L)

        viewModel.cancelTimer()

        assertTrue(viewModel.state.value is TimerState.Idle)
        assertTrue(scheduler.latest().canceled)
    }

    @Test
    fun `stopAndReset sets idle and resets configured duration`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)
        viewModel.startTimer(5_000L)

        viewModel.stopAndReset(7_000L)

        assertTrue(viewModel.state.value is TimerState.Idle)
        assertEquals(7_000L, viewModel.time.value)
    }

    @Test
    fun `onCleared cancels running timer`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = PlayClockViewModel(scheduler)
        viewModel.startTimer(5_000L)

        val method = viewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)

        assertTrue(scheduler.latest().canceled)
    }
}
