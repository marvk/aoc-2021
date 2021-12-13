object Day01 : Day() {
    override val part1 = object : Part<Int>(7) {
        override fun solve(input: List<String>) =
            input
                .asSequence()
                .map { it.toInt() }
                .windowed(2)
                .count { it.component1() < it.component2() }
    }

    override val part2 = object : Part<Int>(5) {
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