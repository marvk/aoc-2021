import kotlin.math.absoluteValue

object Day07 : Day() {
    override val part1 = object : Part<Int>(37) {
        override fun solve(input: List<String>) =
            cheapestOutcome(input)
    }

    override val part2 = object : Part<Int>(168) {
        override fun solve(input: List<String>) =
            cheapestOutcome(input, ::triangularNumber)

        private fun triangularNumber(i: Int) =
            (i * (i + 1)) / 2
    }

    private fun cheapestOutcome(input: List<String>, costMapper: (Int) -> Int = { it }) =
        input
            .single()
            .split(",")
            .map(String::toInt)
            .let { values ->
                IntRange(values.minOf { it }, values.maxOf { it })
                    .minOf { testValue -> values.sumOf { it.minus(testValue).absoluteValue.let(costMapper) } }
            }
}