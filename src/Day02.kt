object Day02 : Day<Int>() {
    override fun part1(input: List<String>) =
        input
            .asSequence()
            .map(::parseToPosition)
            .reduce(Position::plus)
            .run { horizontal * depth }

    override fun part2(input: List<String>) =
        input
            .asSequence()
            .map(::parseToPosition)
            .fold(PositionAccumulator(), PositionAccumulator::plus)
            .run { horizontal * depth }

    override val testResult: Int =
        150

    private fun parseToPosition(line: String): Position =
        line
            .split(" ")
            .let { (direction, deltaString) -> Pair(direction, deltaString.toInt()) }
            .let { (direction, delta) ->
                when (direction) {
                    "forward" -> Position(delta, 0)
                    "down" -> Position(0, delta)
                    "up" -> Position(0, -delta)
                    else -> throw IllegalArgumentException("Unexpected direction $direction, should be one of forward, up or down")
                }
            }

    private data class Position(
        val horizontal: Int,
        val depth: Int,
    ) {
        operator fun plus(other: Position): Position =
            Position(horizontal + other.horizontal, depth + other.depth)
    }

    private data class PositionAccumulator(
        val horizontal: Int = 0,
        val depth: Int = 0,
        private val aim: Int = 0,
    ) {
        operator fun plus(other: Position): PositionAccumulator =
            PositionAccumulator(
                horizontal + other.horizontal,
                depth + aim * other.horizontal,
                aim + other.depth
            )
    }
}

fun main() {
    Day02.run()
}