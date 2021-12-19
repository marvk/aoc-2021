import kotlin.math.absoluteValue

object Day19 : Day() {
    override val part1 = object : Part<Int>(79) {
        override fun solve(input: List<String>) =
            Scanner
                .parseAll(input)
                .let(::doTheThing)
                .asSequence()
                .flatMap { it.second }
                .distinct()
                .count()
    }

    override val part2 = object : Part<Int>(3621) {
        override fun solve(input: List<String>): Int {
            val map = Scanner
                .parseAll(input)
                .let(::doTheThing)
                .map { it.first }

            return map.maxOf { first ->
                map.maxOf { second ->
                    println("$first $second ${first.difference(second).manhattanDistance}")

                    first.difference(second).manhattanDistance
                }
            }

        }

    }

    fun doTheThing(scanners: List<Scanner>): MutableSet<Pair<Vec3, Set<Vec3>>> {
        val first = scanners.first()
        val todo = scanners.drop(1).let(::ArrayDeque)

        var result = mutableSetOf(Vec3.ZERO to first.beacons)

        while (todo.isNotEmpty()) {
            println("~".repeat(100))
            val current = todo.removeFirst()

            println("id ${current.id}  remaining ${todo.size} result ${result.size}")

            current
                .let {
                    result.asSequence().map { r ->
                        r.second.matchTo(it)?.let {
                            (r.first + it.first) to it.second
                        }
                    }.filter { it != null }.firstOrNull()
                }
                ?.let {
                    result.add(it)
                }
                ?: todo.add(current)

        }

        return result
    }

    fun rotations(beacons: Set<Vec3>): Set<Set<Vec3>> {
        var beacons = beacons
        return buildSet {
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
    }

    fun Iterable<Vec3>.matchTo(other: Scanner): Pair<Vec3, Set<Vec3>>? {
        println("matchTo")

        rotations(other.beacons).forEach { someRotationOfOther ->
            val distances =
                this
                    .associateWith { current ->
                        someRotationOfOther.map(current::difference).toSet()
                    }

            val allDistances = distances.values.flatten().toSet()

            allDistances
                .filter { current ->
                    distances.filter { it.value.contains(current) }.size.also { if (it > 1) println("FOUND MATCH WITH $it") } >= 12
                }
                .firstOrNull()
                ?.let { current ->
                    println("YEET")
                    val result = someRotationOfOther.map { it.translate(current) }.toSet()

                    check(this.intersect(result).size >= 12)

                    return current to result
                }
        }

        return null
    }


    data class Scanner(
        val id: Int,
        val beacons: Set<Vec3>,
        var translation: Vec3 = Vec3.ZERO,
    ) {
        companion object {
            fun parseAll(input: List<String>): List<Scanner> {
                return input
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
            }

            fun parse(input: List<String>) =
                Scanner(
                    input.first().replace(Regex("""\D"""), "").toInt(),
                    input.drop(1).map(Vec3.Companion::parse).toSet()
                )
        }
    }

    data class Vec3(
        val x: Int,
        val y: Int,
        val z: Int,
    ) {
        operator fun plus(other: Vec3) = translate(other)
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
