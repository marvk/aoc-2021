import kotlin.math.absoluteValue

object Day19 : Day() {
    override val part1 = object : Part<Int>(79) {
        override fun solve(input: List<String>) =
            Scanner
                .parseAll(input)
                .let(::positionScanners)
                .asSequence()
                .flatMap { it.beacons }
                .distinct()
                .count()
    }

    override val part2 = object : Part<Int>(3621) {
        override fun solve(input: List<String>) =
            Scanner
                .parseAll(input)
                .let(::positionScanners)
                .map { it.distance }
                .let { positions ->
                    positions.maxOf { first ->
                        positions.maxOf { second ->
                            first.difference(second).manhattanDistance
                        }
                    }
                }
    }

    private fun positionScanners(scanners: List<Scanner>) =
        buildSet<PositionedScanner> {
            add(PositionedScanner.atOrigin(scanners.first()))

            val todo = scanners.drop(1).let(::ArrayDeque)

            val combinationsTried = mutableSetOf<Pair<Int, Int>>()

            while (todo.isNotEmpty()) {
                todo.removeFirst().let { current ->
                    current
                        .let { itScanner ->
                            this
                                .filter { !combinationsTried.contains(it.id to current.id) }
                                .firstNotNullOfOrNull {
                                    combinationsTried.add(it.id to current.id)
                                    it.beacons.determinePosition(itScanner)
                                }
                        }
                        ?.let {

                            add(it)
                        }
                        ?: todo.add(current)
                }
            }
        }

    private fun rotations(beacons: Set<Vec3>) =
        buildSet {
            var beacons = beacons
            (0 until 2).forEach { _ ->
                (0 until 3).forEach { _ ->
                    beacons = beacons.map(Vec3::roll).toSet().also(::add)
                    (0 until 3).forEach { _ ->
                        beacons = beacons.map(Vec3::turn).toSet().also(::add)
                    }
                }
                beacons = beacons.asSequence().map(Vec3::roll).map(Vec3::turn).map(Vec3::roll).toSet()
            }
        }

    private fun Iterable<Vec3>.determinePosition(other: Scanner) =
        rotations(other.beacons).firstNotNullOfOrNull { someRotationOfOther ->
            map { current ->
                someRotationOfOther.map(current::difference).toSet()
            }.let { distances ->
                distances
                    .flatten()
                    .toSet()
                    .firstOrNull { current ->
                        distances.filter { it.contains(current) }.size >= 12
                    }
                    ?.let { position ->
                        PositionedScanner(
                            other.id,
                            someRotationOfOther.map { it.translate(position) }.toSet(),
                            position
                        )
                    }
            }
        }

    private data class PositionedScanner(
        val id: Int,
        val beacons: Set<Vec3>,
        val distance: Vec3,
    ) {
        companion object {
            fun atOrigin(scanner: Scanner) = PositionedScanner(scanner.id, scanner.beacons, Vec3.ZERO)
        }
    }

    private data class Scanner(
        val id: Int,
        val beacons: Set<Vec3>,
    ) {
        val rotations by lazy { rotations(beacons) }

        companion object {
            fun parseAll(input: List<String>) =
                input
                    .asSequence()
                    .let { sequence ->
                        buildList<List<String>> {
                            var current = mutableListOf<String>()
                            for (element in sequence) {
                                if (element.isNotEmpty()) {
                                    current.add(element)
                                } else {
                                    add(current)
                                    current = mutableListOf()
                                }
                            }
                            add(current)
                        }
                    }
                    .map(::parse)

            fun parse(input: List<String>) =
                Scanner(
                    input.first().replace(Regex("""\D"""), "").toInt(),
                    input.drop(1).map(Vec3.Companion::parse).toSet()
                )
        }
    }

    private data class Vec3(
        val x: Int,
        val y: Int,
        val z: Int,
    ) {
        operator fun plus(other: Vec3) = translate(other)
        operator fun unaryMinus() = Vec3(-x, -y, -z)
        operator fun minus(other: Vec3) = translate(-other)

        fun translate(other: Vec3) = translate(other.x, other.y, other.z)
        fun translate(dx: Int, dy: Int, dz: Int) = Vec3(x + dx, y + dy, z + dz)
        fun translateX(dx: Int) = translate(dx, 0, 0)
        fun translateY(dy: Int) = translate(0, dy, 0)
        fun translateZ(dz: Int) = translate(9, 0, dz)

        fun difference(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)

        val manhattanDistance = x.absoluteValue + y.absoluteValue + z.absoluteValue

        fun rotateXForwards() = Vec3(x, z, -y)
        fun rotateXBackwards() = Vec3(x, -z, y)

        fun rotateYForwards() = Vec3(z, y, -x)
        fun rotateYBackwards() = Vec3(-z, y, x)

        fun rotateZForwards() = Vec3(y, -x, z)
        fun rotateZBackwards() = Vec3(-y, x, z)

        fun roll() = rotateXForwards()
        fun turn() = rotateZBackwards()

        override fun toString() = "[$x,$y,$z]"

        companion object {
            val ZERO = Vec3(0, 0, 0)

            fun parse(input: String) =
                input
                    .split(",")
                    .map(String::toInt)
                    .let { (x, y, z) -> Vec3(x, y, z) }
        }
    }
}