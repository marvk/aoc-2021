import StopWatch.Companion.runTimed
import java.io.File

abstract class Day {
    protected abstract val part1: Part<*>
    protected abstract val part2: Part<*>

    private val name = this::class.simpleName!!
    private val testInput: List<String> by lazy { readInput("${name}_test") }
    private val input: List<String> by lazy { readInput(name) }

    fun runTest() {
        part1.runTest(1)
        part2.runTest(2)
    }

    fun runActual() {
        part1.runActual(1)
        part2.runActual(2)
    }

    fun run() {
        part1.run(1)
        part2.run(2)
    }

    private fun readInput(name: String) =
        File("src", "$name.txt").readLines()

    protected abstract inner class Part<Result> constructor(private val testExpected: Result) {
        abstract fun solve(input: List<String>): Result

        fun runTest(id: Int) {
            runTimed {
                val testOutput = solve(testInput)
                check(testOutput == testExpected) {
                    "Part $id test failed: Expected $testExpected but was $testOutput\t${stopAndFormat()}"
                }
                println("Part $id test was ${"successful".colored(32)}\t${stopAndFormat()}")
            }
        }

        fun runActual(id: Int) {
            runTimed {
                val result = solve(input).toString().let { if (it.lines().size > 1) "\n" + it else it }
                println("Part $id output ${" ".repeat(0.coerceAtLeast(12 - result.length))}${result.colored(34)}\t${stopAndFormat()}")
            }
        }

        fun run(id: Int) {
            runTest(id)
            runActual(id)
        }
    }
}