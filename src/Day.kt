import java.io.File

abstract class Day<Result> {
    protected abstract val part1: Part<Result>
    protected abstract val part2: Part<Result>

    private val name = this::class.simpleName!!
    private val testInput: List<String> by lazy { readInput("${name}_test") }
    private val input: List<String> by lazy { readInput(name) }

    fun runTests() {
        part1.runTest(1)
        part2.runTest(2)
    }

    fun runActual() {
        part1.runActual()
        part2.runActual()
    }

    fun run() {
        runTests()
        runActual()
    }

    private fun readInput(name: String) =
        File("src", "$name.txt").readLines()

    protected fun part(testExpected: Int, function: (input: List<String>) -> Result) =
        Part(testInput, input, testExpected, function)

    protected class Part<Result> constructor(
        private val testInput: List<String>,
        private val input: List<String>,
        private val testExpected: Int,
        private val function: (input: List<String>) -> Result,
    ) {
        fun runTest(id: Int) {
            val testOutput = function(testInput)
            check(testOutput == testExpected) {
                "Part$id test failed: Expected $testExpected but was $testOutput"
            }
            println("Part$id test completed successfully")
        }

        fun runActual() {
            println(function(input))
        }
    }
}