package yt.javi.gridirontimer.presentation.viewmodel

class FakeCountdownScheduler : CountdownScheduler {
    val timers = mutableListOf<FakeCountdownHandle>()

    override fun create(
        durationMs: Long,
        intervalMs: Long,
        onTick: (Long) -> Unit,
        onFinish: () -> Unit
    ): CountdownHandle {
        val handle = FakeCountdownHandle(durationMs, intervalMs, onTick, onFinish)
        timers.add(handle)
        return handle
    }

    fun latest(): FakeCountdownHandle = timers.last()
}

class FakeCountdownHandle(
    private val durationMs: Long,
    private val intervalMs: Long,
    private val onTick: (Long) -> Unit,
    private val onFinish: () -> Unit
) : CountdownHandle {
    var started = false
        private set
    var canceled = false
        private set
    var remainingMs = durationMs
        private set

    override fun start() {
        started = true
    }

    override fun cancel() {
        canceled = true
    }

    fun emitTick(remaining: Long) {
        remainingMs = remaining
        onTick(remaining)
    }

    fun finish() {
        remainingMs = 0L
        onFinish()
    }

    fun tickAndMaybeFinish(stepMs: Long = intervalMs) {
        remainingMs = (remainingMs - stepMs).coerceAtLeast(0L)
        if (remainingMs == 0L) {
            onFinish()
        } else {
            onTick(remainingMs)
        }
    }
}
