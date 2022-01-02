import StopWatch.Companion.runTimed
import java.io.File

abstract class Day {
    abstract val part1: Part<*>
    abstract val part2: Part<*>

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

    abstract inner class Part<Result> constructor(private val testExpected: Result, val skipTest: Boolean = false) {
        abstract fun solve(input: List<String>): Result

        fun runTest(id: Int) {
            if (skipTest) {
                println("Part $id test ${"skipped".colored(30)}")
            } else {
                runTimed {
                    val testOutput = solve(testInput)
                    check(testOutput == testExpected) {
                        "Part $id test failed: Expected $testExpected but was $testOutput\t${stopAndFormat()}"
                    }
                    println("Part $id test was ${"successful".colored(32)}\t${stopAndFormat()}")
                }
            }
        }

        fun runActual(id: Int) {
            runTimed {
                val rawResult = solve(input).toString()
                val multiline = rawResult.lines().size > 1
                val result = rawResult.colored(34)
                val resultAndTime = if (multiline) {
                    " ".repeat(12) + "\t${stopAndFormat()}\n$result"
                } else {
                    " ".repeat(0.coerceAtLeast(12 - rawResult.length)) + "${result}\t${stopAndFormat()}"
                }
                println("Part $id output $resultAndTime")
            }
        }

        fun run(id: Int) {
            runTest(id)
            runActual(id)
        }
    }
}