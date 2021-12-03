object Day01 : Day<Int>() {
    override val part1 = part(7) { input ->
        input
            .asSequence()
            .map { it.toInt() }
            .windowed(2)
            .count { it.component1() < it.component2() }
    }

    override val part2 = part(5) { input ->
        input
            .asSequence()
            .map { it.toInt() }
            .windowed(3)
            .map { it.sum() }
            .windowed(2)
            .count { it.component1() < it.component2() }
    }
}