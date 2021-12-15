import java.time.Duration

class StopWatch {
    var startTime: Long? = null
        private set
    var started = false
        private set
    var duration: Duration? = null
        private set

    private fun now() = System.nanoTime()

    fun start() {
        if (started) {
            throw IllegalStateException()
        }
        started = true
        startTime = now()
    }

    fun stop(): Duration {
        if (!started) {
            throw IllegalStateException()
        }
        if (duration == null) {
            duration = Duration.ofNanos(now() - startTime!!)
        }
        return duration!!
    }

    companion object {
        fun runTimed(block: Scope.() -> Unit) {
            Scope(StopWatch().apply(StopWatch::start)).block()
        }
    }

    class Scope(private val stopWatch: StopWatch) {
        fun stop(): Duration {
            return stopWatch.stop()
        }

        fun stopAndFormat(): String {
            return stop().toString().colored(30)
        }
    }
}
