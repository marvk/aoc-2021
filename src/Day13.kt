object Day13 : Day<Int>() {
    override val part1 = object : Part(17) {
        override fun solve(input: List<String>): Int {
            return Paper.parse(input).foldOnce().points.size
        }
    }

    override val part2 = object : Part(16) {
        override fun solve(input: List<String>): Int {
            return Paper.parse(input).foldAll().points.size
        }
    }

    data class Paper(
        val points: Set<Point>,
        val foldInstructions: List<FoldInstruction>,
    ) {
        companion object {
            fun parse(input: List<String>): Paper {
                val (rawFoldInstructions, rawDots) =
                    input
                        .filter(String::isNotEmpty)
                        .partition { it.contains("fold along") }


                val foldInstructions =
                    rawFoldInstructions
                        .map { it.split(" ") }
                        .map(List<String>::component3)
                        .map { it.split("=") }
                        .map { (axis, coordinate) ->
                            FoldInstruction(coordinate.toInt(), FoldInstruction.Axis.parse(axis))
                        }

                val points =
                    rawDots
                        .map { it.split(",") }
                        .map { it.map(String::toInt) }
                        .map { (x, y) -> Point(x, y) }
                        .toSet()

                return Paper(points, foldInstructions)
            }
        }

        fun foldAll(): Paper {
            var result = this

            while (result.foldInstructions.isNotEmpty()) {
                result = result.foldOnce()
            }

            return result
        }

        fun foldOnce(): Paper {
            val foldInstruction = foldInstructions.first()

            val pointsToFold = foldInstruction.mirror(points)

            val (toFold, remainder) = pointsToFold.partition { it.x >= foldInstruction.coordinate }

            return Paper(
                toFold.map { Point(foldInstruction.coordinate - (it.x - foldInstruction.coordinate), it.y) }
                    .plus(remainder).toSet().let(foldInstruction::mirror),
                foldInstructions.drop(1)
            )
        }

        override fun toString(): String {
            val maxX = points.maxOf(Point::x)
            val maxY = points.maxOf(Point::y)

            val chars = Array(maxY + 1) {
                CharArray(maxX + 1) {
                    '.'
                }
            }

            points.map { Point(it.x, it.y) }.forEach {
                chars[it.y][it.x] = '#'
            }

            return chars.joinToString("\n") { it.joinToString("") }.let { "\n$it\n" }
        }
    }

    private fun mirrorDiagonally(points: Set<Point>) = points.map { (x, y) -> Point(y, x) }.toSet()

    data class Point(
        val x: Int,
        val y: Int,
    )

    data class FoldInstruction(val coordinate: Int, val axis: Axis) {
        fun mirror(points: Set<Point>): Set<Point> {
            return if (axis == Axis.X) points else mirrorDiagonally(points)
        }

        enum class Axis {
            X, Y;

            companion object {
                fun parse(string: String) =
                    when (string.lowercase()) {
                        "x" -> X
                        "y" -> Y
                        else -> throw IllegalArgumentException()
                    }
            }
        }
    }
}

