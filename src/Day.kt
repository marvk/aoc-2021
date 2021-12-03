import java.io.File

abstract class Day<Result> {
    protected abstract val part1: Part
    protected abstract val part2: Part

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


    protected abstract inner class Part constructor(private val testExpected: Int) {
        abstract fun solve(input: List<String>): Result

        fun runTest(id: Int) {
            val testOutput = solve(testInput)
            check(testOutput == testExpected) {
                "Part $id test failed: Expected $testExpected but was $testOutput"
            }
            println("Part $id test was \u001B[32msuccessful\u001B[0m")
        }

        fun runActual(id: Int) {
            val result = solve(input).toString()
            println("Part $id output ${" ".repeat(12 - result.length)}\u001B[34m$result\u001B[0m")
        }

        fun run(id: Int) {
            runTest(id)
            runActual(id)
        }
    }
}