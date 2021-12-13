object Day13 : Day() {
    override val part1 = object : Part<Int>(17) {
        override fun solve(input: List<String>) =
            Paper.parse(input).foldOnce().dots.size
    }

    override val part2 = object : Part<String>("""
    #####
    #...#
    #...#
    #...#
    #####
    """.trimIndent()) {
        override fun solve(input: List<String>) =
            Paper.parse(input).foldAll().stringRepresentation()
    }

    data class Paper(
        val dots: Set<Dot>,
        val foldInstructions: List<FoldInstruction>,
    ) {
        companion object {
            fun parse(input: List<String>) =
                splitIntoSections(input)
                    .let { parseDots(it.second) to parseFoldInstructions(it.first) }
                    .let { Paper(it.first, it.second) }

            private fun splitIntoSections(input: List<String>) =
                input
                    .filter(String::isNotEmpty)
                    .partition { it.contains("fold along") }

            private fun parseDots(input: List<String>) =
                input
                    .map { it.split(",") }
                    .map { it.map(String::toInt) }
                    .map { (x, y) -> Dot(x, y) }
                    .toSet()

            private fun parseFoldInstructions(input: List<String>) =
                input
                    .map { it.split(" ") }
                    .map(List<String>::component3)
                    .map { it.split("=") }
                    .map { (axis, coordinate) ->
                        FoldInstruction(coordinate.toInt(), FoldInstruction.Axis.parse(axis))
                    }
        }

        fun foldAll() =
            generateSequence(this, Paper::foldOnce)
                .takeWhile { it.foldInstructions.isNotEmpty() }
                .last()
                .foldOnce()

        fun foldOnce() =
            Paper(
                foldInstructions.first().fold(dots),
                foldInstructions.drop(1)
            )

        fun stringRepresentation(): String =
            Array(dots.maxOf(Dot::y) + 1) {
                CharArray(dots.maxOf(Dot::x) + 1) {
                    '.'
                }
            }.apply {
                dots.map { Dot(it.x, it.y) }.forEach {
                    this[it.y][it.x] = '#'
                }
            }.joinToString("\n") {
                it.joinToString("")
            }
    }

    private fun mirrorDiagonally(dots: Iterable<Dot>) =
        dots.map { (x, y) -> Dot(y, x) }.toSet()

    data class Dot(val x: Int, val y: Int)

    data class FoldInstruction(val coordinate: Int, val axis: Axis) {
        fun fold(dots: Set<Dot>) =
            mirrorIfRequired(dots)
                .partition { it.x >= coordinate }
                .let { (toFold, remainder) ->
                    toFold
                        .map { Dot(coordinate - (it.x - coordinate), it.y) }
                        .plus(remainder)
                }
                .let(::mirrorIfRequired)
                .toSet()

        private fun mirrorIfRequired(dots: Iterable<Dot>) =
            if (axis == Axis.X) dots else mirrorDiagonally(dots)

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

