object Day11 : Day() {
    override val part1 = object : Part<Int>(1656) {
        override fun solve(input: List<String>) =
            Cavern
                .parse(input)
                .let { cavern ->
                    (1..100)
                        .sumOf { cavern.simulateDay() }
                }
    }

    override val part2 = object : Part<Int>(195) {
        override fun solve(input: List<String>) =
            Cavern
                .parse(input)
                .let { cavern ->
                    (1..Integer.MAX_VALUE)
                        .first { cavern.simulateDay() == cavern.size }
                }
    }

    private class Cavern(val energyLevels: Array<IntArray>) {
        val width = energyLevels[0].size
        val height = energyLevels.size
        val size = width * height

        fun simulateDay() =
            incrementAll()
                .let { flashAll() }
                .also { resetAllFlashed() }

        private fun resetAllFlashed() {
            iterate { x, y, value ->
                if (value < 0) {
                    set(x, y, 0)
                }
            }
        }

        private fun incrementAll() {
            iterate { x, y, value ->
                set(x, y, value + 1)
            }
        }

        private fun flashAll() =
            generateSequence(::flash).takeWhile { it > 0 }.sum()

        private fun flash() =
            iterate { x, y, value ->
                when {
                    value > 9 -> incrementNeighbours(x, y).let { x to y }
                    else -> null
                }
            }
                .flatten()
                .filterNotNull()
                .onEach {
                    set(it.first, it.second, Integer.MIN_VALUE)
                }.count()

        private fun incrementNeighbours(x: Int, y: Int) {
            increment(x + 1, y)
            increment(x - 1, y)
            increment(x + 1, y + 1)
            increment(x - 1, y - 1)
            increment(x, y + 1)
            increment(x, y - 1)
            increment(x + 1, y - 1)
            increment(x - 1, y + 1)
        }

        private fun increment(x: Int, y: Int) {
            set(x, y, get(x, y)?.plus(1))
        }

        private fun set(x: Int, y: Int, value: Int?) {
            if (get(x, y) != null && value != null) {
                energyLevels[y][x] = value
            }
        }

        private fun get(x: Int, y: Int) = energyLevels.getOrNull(y)?.getOrNull(x)

        private fun <T> iterate(function: (x: Int, y: Int, value: Int) -> T) =
            (0 until height).map { y ->
                (0 until width).map { x ->
                    function(x, y, get(x, y)!!)
                }
            }

        companion object {
            fun parse(input: List<String>) =
                input
                    .map(::parseLine)
                    .toTypedArray()
                    .let(::Cavern)


            private fun parseLine(line: String) =
                line
                    .chunked(1)
                    .map(String::toInt)
                    .toIntArray()
        }

    }
}