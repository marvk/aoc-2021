object Day06 : Day<Long>() {
    override val part1 = object : Part(5934L) {
        override fun solve(input: List<String>): Long =
            solve(input, 80)
    }

    override val part2 = object : Part(26984457539L) {
        override fun solve(input: List<String>): Long =
            solve(input, 256)
    }

    private fun solve(input: List<String>, timesToGrow: Int) =
        input
            .single()
            .split(",")
            .map(String::toInt)
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toLong() }
            .let { growNTimes(it, timesToGrow) }
            .map(Map.Entry<Int, Long>::value)
            .sum()

    private fun growNTimes(ageToFishes: Map<Int, Long>, n: Int) =
        (0 until n)
            .fold(ageToFishes) { acc, _ -> grow(acc) }

    private fun grow(ageToFishes: Map<Int, Long>): Map<Int, Long> =
        ageToFishes
            .mapKeys { it.key - 1 }
            .toMutableMap()
            .apply {
                set(8, ageToFishes.getOrDefault(0, 0))
                set(6, getOrDefault(-1, 0) + getOrDefault(6, 0))
                remove(-1)
            }
}