object Day01 : Day<Int>() {
    override val part1 = object : Part(7) {
        override fun solve(input: List<String>) =
            input
                .asSequence()
                .map { it.toInt() }
                .windowed(2)
                .count { it.component1() < it.component2() }
    }

    override val part2 = object : Part(5) {
        override fun solve(input: List<String>) =
            input
                .asSequence()
                .map { it.toInt() }
                .windowed(3)
                .map { it.sum() }
                .windowed(2)
                .count { it.component1() < it.component2() }
    }
}