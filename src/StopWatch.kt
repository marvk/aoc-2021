import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

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
            duration = (now() - startTime!!).nanoseconds
        }
        return duration!!
    }

    companion object {
        fun runTimed(block: Scope.() -> Unit) =
            Scope(StopWatch().apply(StopWatch::start)).apply(block).run(Scope::stopIfNotStopped)
    }

    class Scope(private val stopWatch: StopWatch) {
        internal fun stopIfNotStopped() = stopWatch.duration ?: stop()

        fun stop() = stopWatch.stop()

        fun stopAndFormat() = stop().toString().colored(30)
    }
}
