object Day20 : Day() {
    override val part1 = object : Part<Int>(35) {
        override fun solve(input: List<String>) = solve(input, 2)
    }
    override val part2 = object : Part<Int>(3351) {
        override fun solve(input: List<String>) = solve(input, 50)
    }

    private fun solve(input: List<String>, count: Int) =
        parse(input).let { (algorithm, image) ->
            image.enhanceNTimes(algorithm, count).count()
        }

    fun parse(input: List<String>) =
        Algorithm(input.first()) to Image.parse(input.drop(2))

    data class Image(
        val data: Map<Vec2, Int>,
        val infiniteGridState: Int,
    ) {
        val minX = data.keys.minOf(Vec2::x) - 1
        val maxX = data.keys.maxOf(Vec2::x) + 1
        val minY = data.keys.minOf(Vec2::y) - 1
        val maxY = data.keys.maxOf(Vec2::y) + 1

        private fun get(vec: Vec2) = data[vec] ?: infiniteGridState
        private fun get(x: Int, y: Int) = get(Vec2(x, y))

        fun count() = data.values.count { it == 1 }

        fun enhanceNTimes(algorithm: Algorithm, count: Int) =
            (0 until count).fold(this) { current, _ ->
                current.enhance(algorithm)
            }

        fun enhance(algorithm: Algorithm): Image {
            return buildMap {
                (minY..maxY).forEach { x ->
                    (minX..maxX).forEach { y ->
                        calculateKernelIndex(x, y)
                            .let(algorithm::lookup)
                            .also {
                                put(Vec2(x, y), it)
                            }
                    }
                }
            }.let {
                Image(it, algorithm.lookup(if (infiniteGridState == 0) 0 else 511))
            }
        }

        private fun calculateKernelIndex(x: Int, y: Int) =
            (y - 1..y + 1).flatMap { cy ->
                (x - 1..x + 1).map { cx ->
                    get(cx, cy)
                }
            }.reduce { acc, int ->
                acc.shl(1).or(int)
            }

        override fun toString() =
            (minY..maxY).joinToString("\n") { y ->
                (minX..maxX).joinToString("") { x ->
                    if (get(x, y) == 0) "." else "#"
                }
            }

        companion object {
            fun parse(input: List<String>) =
                input
                    .flatMapIndexed { y, line ->
                        line.mapIndexed { x, char ->
                            Vec2(x, y) to if (char == '#') 1 else 0
                        }
                    }
                    .toMap()
                    .let {
                        Image(it, 0)
                    }
        }
    }

    data class Algorithm(
        private val mapping: String,
    ) {
        fun lookup(it: Int) = when (mapping[it]) {
            '.' -> 0
            else -> 1
        }
    }

    data class Vec2(
        val x: Int,
        val y: Int,
    )
}