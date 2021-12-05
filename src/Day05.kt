import kotlin.math.sign

object Day05 : Day<Int>() {
    override val part1 = object : Part(5) {
        override fun solve(input: List<String>) =
            solve(input, Line::straightPoints)
    }

    override val part2 = object : Part(12) {
        override fun solve(input: List<String>) =
            solve(input, Line::allPoints)
    }

    private fun solve(input: List<String>, lineToPoints: (Line) -> List<Point>) =
        input
            .map(::parseLine)
            .flatMap(lineToPoints)
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .count()

    private fun parseLine(string: String) =
        string
            .split(" -> ")
            .map(::parsePoint)
            .let { (from, to) -> Line(from, to) }

    private fun parsePoint(string: String) =
        string
            .split(",")
            .map(String::toInt)
            .let { (x, y) -> Point(x, y) }

    private data class Line(
        val from: Point,
        val to: Point,
    ) {
        val straightPoints: List<Point> by lazy { from straightLineOrEmpty to }
        val allPoints: List<Point> by lazy { from line to }
    }

    private data class Point(
        val x: Int,
        val y: Int,
    ) {
        infix fun line(other: Point) =
            generateSequence(this) { it + Point((other.x - x).sign, (other.y - y).sign) }
                .takeWhile { it != other }
                .plus(other)
                .toList()

        infix fun straightLineOrEmpty(other: Point) = when {
            x == other.x || y == other.y -> line(other)
            else -> listOf()
        }

        operator fun plus(other: Point) =
            Point(x + other.x, y + other.y)
    }
}