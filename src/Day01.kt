object Day01 : Day<Int>() {
    override fun part1(input: List<String>) =
        input
            .asSequence()
            .map { it.toInt() }
            .windowed(2)
            .count { it.component1() < it.component2() }

    override fun part2(input: List<String>) =
        input
            .asSequence()
            .map { it.toInt() }
            .windowed(3)
            .map { it.sum() }
            .windowed(2)
            .count { it.component1() < it.component2() }

    override val testResult: Int = 7
}

fun main() {
    Day01.run()
}