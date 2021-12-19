import Day19.Vec3
import kotlin.math.absoluteValue

object Day19 : Day() {
    override val part1 = object : Part<Int>(79) {
        override fun solve(input: List<String>) =
            Scanner
                .parseAll(input)
                .let(::doTheThing)
                .asSequence()
                .flatMap { it.beacons }
                .distinct()
                .count()
    }

    override val part2 = object : Part<Int>(3621) {
        override fun solve(input: List<String>): Int {
            val map = Scanner
                .parseAll(input)
                .let(::doTheThing)
                .map { it.distance }

            return map.maxOf { first ->
                map.maxOf { second ->
                    println("$first $second ${first.difference(second).manhattanDistance}")

                    first.difference(second).manhattanDistance
                }
            }

        }

    }

    fun doTheThing(scanners: List<Scanner>): Set<MatchResult> {
        val first = scanners.first()
        val todo = scanners.drop(1).let(::ArrayDeque)

        var result = mutableSetOf(MatchResult(first.beacons, Vec3.ZERO, Rotation.IDENTITY))

        while (todo.isNotEmpty()) {
            val current = todo.removeFirst()

            current
                .let { itScanner ->
                    result.asSequence().map { r ->
                        r.beacons.matchTo(itScanner)?.let {
                            println("<>~~~~~~~~~~~~~~~~~~~<> ${itScanner.id} ~~ ${it.distance}")
                            it.distance to it.beacons
                        }
                    }.filter { it != null }.firstOrNull()
                }
                ?.let {
                    println("${current.id} + ${it.first}")

                    result.add(MatchResult(it.second, it.first, Rotation.IDENTITY))
                }
                ?: todo.add(current)

        }

        return result
    }

    fun rotations(beacons: Set<Vec3>): Set<Pair<Rotation, Set<Vec3>>> {
        return Rotation.ALL.map { sequence ->
            sequence to beacons.asSequence().map { it.rotate(sequence) }.toSet()
        }.toSet()
    }

    private fun Iterable<Vec3>.matchTo(other: Scanner): MatchResult? {
        rotations(other.beacons).forEach { someRotationOfOther ->
            val distances =
                this
                    .associateWith { current ->
                        someRotationOfOther.second.map(current::difference).toSet()
                    }

            val allDistances = distances.values.flatten().toSet()

            allDistances
                .filter { current ->
                    distances.filter { it.value.contains(current) }.size >= 12
                }
                .firstOrNull()
                ?.let { current ->
                    return MatchResult(
                        someRotationOfOther.second.map { it.translate(current) }.toSet(),
                        current,
                        someRotationOfOther.first
                    )
                }
        }

        return null
    }

    data class MatchResult(
        val beacons: Set<Vec3>,
        val distance: Vec3,
        val rotation: Rotation,
    )

    data class Scanner(
        val id: Int,
        val beacons: Set<Vec3>,
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

    @JvmInline
    value class Rotation(private val sequence: String) {
        private val reverseSequence
            get() = sequence.reversed()

        init {
            check(sequence.matches(Regex("""[XY]*""")))
        }

        fun rotate(vec: Vec3): Vec3 {
            var result = vec
            sequence.forEach {
                result = when (it) {
                    'X' -> result.rotateXForwards()
                    'Y' -> result.rotateYForwards()
                    else -> throw IllegalStateException()
                }
            }
            return result
        }

        fun rotateReverse(vec: Vec3): Vec3 {
            var result = vec
            reverseSequence.forEach {
                result = when (it) {
                    'X' -> result.rotateXBackwards()
                    'Y' -> result.rotateYBackwards()
                    else -> throw IllegalStateException()
                }
            }
            return result
        }

        companion object {

            val IDENTITY = Rotation("")

            val ALL = listOf(
                "",
                "X",
                "Y",
                "XX",
                "XY",
                "YX",
                "YY",
                "XXX",
                "XXY",
                "XYX",
                "XYY",
                "YXX",
                "YYX",
                "YYY",
                "XXXY",
                "XXYX",
                "XXYY",
                "XYXX",
                "XYYY",
                "YXXX",
                "YYYX",
                "XXXYX",
                "XYXXX",
                "XYYYX",
            ).map(::Rotation)
        }
    }

    data class Vec3(
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

        fun rotate(rotation: Rotation) = rotation.rotate(this)
        fun rotateReverse(rotation: Rotation) = rotation.rotateReverse(this)
        fun rotateXForwards() = Vec3(x, z, -y)
        fun rotateXBackwards() = Vec3(x, -z, y)

        fun rotateYForwards() = Vec3(z, y, -x)
        fun rotateYBackwards() = Vec3(-z, y, x)

        fun rotateZForwards() = Vec3(y, -x, z)
        fun rotateZBackwards() = Vec3(-y, x, z)

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


fun main() {
    val vec3 = Vec3(1, 2, 3)

    check(vec3 == vec3.rotateXForwards().rotateXBackwards())
    check(vec3 == vec3.rotateYForwards().rotateYBackwards())
    check(vec3 == vec3.rotateZForwards().rotateZBackwards())
}