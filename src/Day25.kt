object Day25 : Day() {
    override val part1 = object : Part<Int>(58) {
        override fun solve(input: List<String>): Int {
            val map = input.map {
                it.toCharArray().map {
                    when (it) {
                        '>' -> Direction.RIGHT
                        'v' -> Direction.DOWN
                        else -> null
                    }
                }
            }.let(::Map)

            return generateSequence(2) { it + 1 }.takeWhile { map.next() }.last()
        }
    }

    override val part2 = object : Part<Unit>(Unit, skipTest = true) {
        override fun solve(input: List<String>) {}
    }

    class Map(private var raw: List<List<Direction?>>) {

        private val maxX = raw[0].size
        private val maxY = raw.size

        override fun toString() = raw.map { it.map { it.toString() }.joinToString("") }.joinToString("\n")

        fun next(): Boolean {
            val result = raw.map { it.toMutableList() }.toMutableList()

            var moves = 0

            for (direction in Direction.values()) {
                raw.forEachIndexed { y, row ->
                    row.forEachIndexed { x, current ->
                        if (current == direction) {
                            val newX = (x + direction.dx) % maxX
                            val newY = (y + direction.dy) % maxY

                            if (raw[newY][newX] == null) {
                                result[newY][newX] = direction
                                result[y][x] = null
                                moves++
                            }
                        }
                    }
                }

                raw = result.map { it.toList() }.toList()
            }

            return moves != 0
        }
    }

    enum class Direction(val dx: Int, val dy: Int) {
        RIGHT(1, 0),
        DOWN(0, 1),
        ;
    }

    fun Direction?.toString() =
        when (this) {
            null -> '.'
            Direction.DOWN -> 'v'
            Direction.RIGHT -> '>'
        }.toString()
}
