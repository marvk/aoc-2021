object Day09 : Day<Int>() {
    override val part1 = object : Part(15) {
        override fun solve(input: List<String>) = HeightMap
            .parse(input)
            .apply(HeightMap::calculateRiskLevel)
            .sum()
    }

    override val part2 = object : Part(1134) {
        override fun solve(input: List<String>) = HeightMap
            .parse(input)
            .apply(HeightMap::calculateBasinSize)
            .sortedDescending()
            .take(3)
            .reduce(Int::times)
    }

    private data class HeightMap(private val map: List<List<Int>>) {
        init {
            require(map.map(List<Int>::size).distinct().size == 1)
        }

        private val width = map[0].size
        private val height = map.size

        fun <T> apply(function: (HeightMap, Point) -> T?) =
            (0 until height).flatMap { y ->
                (0 until width).mapNotNull { x ->
                    Point(x, y)
                }.mapNotNull { function(this, it) }
            }.toList()

        fun calculateRiskLevel(p: Point) =
            if (isLowPoint(p)) get(p)!! + 1 else null

        fun calculateBasinSize(p: Point) =
            if (isLowPoint(p)) {
                val visited = mutableSetOf<Point>()
                val todo = mutableSetOf(p)

                while (todo.isNotEmpty()) {
                    val current = todo.first()
                    todo.remove(current)
                    if (get(current) != 9) {
                        visited += current
                        todo += neighbours(current) - visited
                    }
                }

                visited.size
            } else {
                null
            }

        private operator fun get(p: Point) =
            if (isValidIndex(p)) map[p.y][p.x] else null

        private fun isLowPoint(p: Point) =
            neighbouringValues(p).none { it <= get(p)!! }

        private fun neighbouringValues(p: Point) =
            neighbours(p).mapNotNull(::get)

        private fun neighbours(p: Point) =
            with(p) {
                listOf(
                    Point(x - 1, y),
                    Point(x, y - 1),
                    Point(x + 1, y),
                    Point(x, y + 1),
                )
            }.filter(::isValidIndex)

        private fun isValidIndex(p: Point) =
            p.x in 0 until width && p.y in 0 until height

        companion object {
            fun parse(input: List<String>) =
                input
                    .map { it.chunked(1).map(String::toInt) }
                    .let(::HeightMap)
        }

        private data class Point(val x: Int, val y: Int)
    }
}