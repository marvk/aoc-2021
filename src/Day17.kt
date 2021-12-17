import kotlin.math.*

object Day17 : Day() {
    override val part1 = object : Part<Int>(45) {
        override fun solve(input: List<String>): Int =
            Input
                .parse(input.single())
                .let(::findHits)
                .map(ShotResult.Hit::trajectory)
                .maxOf { it.maxOf(Vector2::y) }
    }
    override val part2 = object : Part<Int>(112) {
        override fun solve(input: List<String>): Int =
            Input
                .parse(input.single())
                .let(::findHits)
                .toList()
                .count()
    }

    private fun findHits(input: Input) =
        (-100..input.xRange.last)
            .flatMap { y ->
                (0..250)
                    .map { x -> Vector2(x, y) }
            }
            .asSequence()
            .map {
                input.simulate(it)
            }
            .filterIsInstance<ShotResult.Hit>()

    private fun Vector2.stepVelocity() =
        Vector2(x - x.sign, y - 1)

    private fun Input.simulate(initialVelocity: Vector2, steps: Int = 200): ShotResult {
        val trajectory = mutableListOf<Vector2>()

        var currentVelocity = initialVelocity
        var currentPosition = Vector2(0, 0)

        var maxY = currentPosition.y

        for (i in (0 until steps)) {
            trajectory.add(currentPosition)

            if (isInRange(currentPosition)) {
                return ShotResult.Hit(i, trajectory)
            }

            currentPosition += currentVelocity
            currentVelocity = currentVelocity.stepVelocity()
            maxY = max(maxY, currentPosition.y)
        }

        return ShotResult.Miss
    }

    sealed interface ShotResult {
        data class Hit(val steps: Int, val trajectory: List<Vector2>) : ShotResult
        object Miss : ShotResult
    }

    data class Vector2(
        val x: Int,
        val y: Int,
    ) {
        operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    }

    data class Input(
        val xRange: IntRange,
        val yRange: IntRange,
    ) {
        fun isInRange(position: Vector2) =
            xRange.contains(position.x) && yRange.contains(position.y)

        companion object {
            private val PATTERN =
                Regex("""x=(?<xmin>[-]?\d+)..(?<xmax>[-]?\d+), y=(?<ymin>[-]?\d+)..(?<ymax>[-]?\d+)""")

            fun parse(input: String) =
                input
                    .split(":")
                    .let(List<String>::component2)
                    .let(String::trim)
                    .let(PATTERN::matchEntire)
                    .let { it!!.groups }
                    .let { matchGroups ->
                        listOf("xmin", "xmax", "ymin", "ymax")
                            .map { matchGroups[it]!!.value.toInt() }
                    }
                    .let { (xMin, xMax, yMin, yMax) ->
                        Input(IntRange(xMin, xMax), IntRange(yMin, yMax))
                    }
        }
    }
}