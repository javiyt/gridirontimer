package yt.javi.gridirontimer.presentation.views

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import yt.javi.gridirontimer.presentation.viewmodel.PlayClockViewModel
import yt.javi.gridirontimer.presentation.viewmodel.TimerState
import yt.javi.gridirontimer.presentation.viewmodel.TimerViewModel
import yt.javi.gridirontimer.presentation.viewmodel.TimeoutViewModel
import yt.javi.gridirontimer.presentation.viewmodel.FakeCountdownScheduler

class GameAndPlayClockScreenTest {

    @Test
    fun `GameAndPlayClockScreenState holds all required state properties`() {
        val scheduler = FakeCountdownScheduler()
        val gameClockViewModel = TimerViewModel(scheduler)
        val playClockViewModel = PlayClockViewModel(scheduler)
        val timeoutViewModel = TimeoutViewModel(scheduler)

        val state = GameAndPlayClockScreenState(
            gameClockRemaining = 1200_000L,
            gameClockState = TimerState.Running,
            gameClockViewModel = gameClockViewModel,
            playClockViewModel = playClockViewModel,
            playClockState = TimerState.Idle,
            playClockRemaining = 25_000L,
            timeoutViewModel = timeoutViewModel
        )

        assertEquals(1200_000L, state.gameClockRemaining)
        assertEquals(TimerState.Running, state.gameClockState)
        assertEquals(gameClockViewModel, state.gameClockViewModel)
        assertEquals(playClockViewModel, state.playClockViewModel)
        assertEquals(TimerState.Idle, state.playClockState)
        assertEquals(25_000L, state.playClockRemaining)
        assertEquals(timeoutViewModel, state.timeoutViewModel)
    }

    @Test
    fun `GameAndPlayClockScreenCallbacks holds all callback functions`() {
        var flagClock25Called = false
        var sevenSecondCalled = false
        var resetFlagCalled = false

        val callbacks = GameAndPlayClockScreenCallbacks(
            startFlagPlayClock25 = { flagClock25Called = true },
            startSevenSecondClock = { sevenSecondCalled = true },
            resetFlagTimers = { resetFlagCalled = true }
        )

        callbacks.startFlagPlayClock25()
        callbacks.startSevenSecondClock()
        callbacks.resetFlagTimers()

        assert(flagClock25Called)
        assert(sevenSecondCalled)
        assert(resetFlagCalled)
    }

    @Test
    fun `GameAndPlayClockScreenCallbacks are independent`() {
        var flag25Called = false
        var sevenSecondCalled = false

        val callbacks1 = GameAndPlayClockScreenCallbacks(
            startFlagPlayClock25 = { flag25Called = true },
            startSevenSecondClock = { sevenSecondCalled = true },
            resetFlagTimers = {}
        )

        callbacks1.startFlagPlayClock25()
        callbacks1.startSevenSecondClock()

        assert(flag25Called)
        assert(sevenSecondCalled)
    }

    @Test
    fun `TimerScreenViewModels contains all required ViewModels`() {
        val scheduler = FakeCountdownScheduler()
        val gameClockViewModel = TimerViewModel(scheduler)
        val playClockViewModel = PlayClockViewModel(scheduler)
        val sevenSecondViewModel = PlayClockViewModel(scheduler)
        val timeoutViewModel = TimeoutViewModel(scheduler)

        val viewModels = TimerScreenViewModels(
            gameClockViewModel = gameClockViewModel,
            playClockViewModel = playClockViewModel,
            sevenSecondClockViewModel = sevenSecondViewModel,
            timeoutViewModel = timeoutViewModel
        )

        assertNotNull(viewModels.gameClockViewModel)
        assertNotNull(viewModels.playClockViewModel)
        assertNotNull(viewModels.sevenSecondClockViewModel)
        assertNotNull(viewModels.timeoutViewModel)

        assertEquals(gameClockViewModel, viewModels.gameClockViewModel)
        assertEquals(playClockViewModel, viewModels.playClockViewModel)
        assertEquals(sevenSecondViewModel, viewModels.sevenSecondClockViewModel)
        assertEquals(timeoutViewModel, viewModels.timeoutViewModel)
    }

    @Test
    fun `GameAndPlayClockScreenState can be created with various timer states`() {
        val scheduler = FakeCountdownScheduler()
        val viewModel = TimerViewModel(scheduler)
        val playClockViewModel = PlayClockViewModel(scheduler)
        val timeoutViewModel = TimeoutViewModel(scheduler)

        val statesAndRemainingTimes = listOf(
            TimerState.Running to 600_000L,
            TimerState.Paused to 300_000L,
            TimerState.Idle to 1200_000L,
            TimerState.Finished to 0L
        )

        statesAndRemainingTimes.forEach { (state, remaining) ->
            val screenState = GameAndPlayClockScreenState(
                gameClockRemaining = remaining,
                gameClockState = state,
                gameClockViewModel = viewModel,
                playClockViewModel = playClockViewModel,
                playClockState = TimerState.Idle,
                playClockRemaining = 0L,
                timeoutViewModel = timeoutViewModel
            )

            assertEquals(state, screenState.gameClockState)
            assertEquals(remaining, screenState.gameClockRemaining)
        }
    }
}
