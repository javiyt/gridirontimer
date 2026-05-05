package yt.javi.gridirontimer.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerViewModelTest {

    @Test
    fun `startTimer creates and starts countdown handle`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)

        viewModel.startTimer(2_000L)
        assertTrue(viewModel.state.value is TimerState.Running)
        assertTrue(scheduler.latest().started)
    }

    @Test
    fun `onTick updates remaining time`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)

        viewModel.startTimer(5_000L)
        scheduler.latest().emitTick(3_000L)

        assertEquals(3_000L, viewModel.time.value)
        assertTrue(viewModel.state.value is TimerState.Running)
    }

    @Test
    fun `finish updates state and time`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)

        viewModel.startTimer(2_000L)
        scheduler.latest().finish()

        assertEquals(0L, viewModel.time.value)
        assertTrue(viewModel.state.value is TimerState.Finished)
    }

    @Test
    fun `pauseTimer sets paused and resumeTimer creates new countdown from remaining`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)
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
    fun `onTick with zero remaining sets finished state`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)

        viewModel.startTimer(1_000L)
        scheduler.latest().emitTick(0L)

        assertTrue(viewModel.state.value is TimerState.Finished)
    }

    @Test
    fun `resumeTimer when not paused sets running state`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)

        viewModel.resumeTimer()

        assertTrue(viewModel.state.value is TimerState.Running)
        assertTrue(scheduler.timers.isEmpty())
    }

    @Test
    fun `onCleared cancels running timer`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)
        viewModel.startTimer(5_000L)

        val method = viewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)

        assertTrue(scheduler.latest().canceled)
    }
}
