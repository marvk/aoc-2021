abstract class Day<Result> {
    abstract fun part1(input: List<String>): Result
    abstract fun part2(input: List<String>): Result
    protected abstract val testResult: Result
    private val name = this::class.simpleName!!

    fun run() {
        val testInput = readInput("${name}_test")
        check(part1(testInput) == testResult)

        val input = readInput(name)
        println(part1(input))
        println(part2(input))
    }
}