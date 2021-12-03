abstract class Day<Result> {
    abstract fun part1(input: List<String>): Result
    abstract fun part2(input: List<String>): Result
    protected abstract val testResult: Result
    protected open val testResult2: Result
        get() = TODO("Not yet implemented")
    private val name = this::class.simpleName!!

    fun run() {
        val testInput = readInput("${name}_test")

        val testOutput = part1(testInput)
        check(testOutput == testResult) {
            "Expected $testResult but was $testOutput"
        }


        try {
            val testOutput2 = part2(testInput)
            check(testOutput2 == testResult2) {
                "Expected $testResult2 but was $testOutput2"
            }
        } catch (_: NotImplementedError) {
        }

        val input = readInput(name)
        println(part1(input))
        println(part2(input))
    }
}