import kotlin.math.absoluteValue

object Day19 : Day() {
    override val part1 = object : Part<Int>(127) {
        override fun solve(input: List<String>): Int {
            val parseAll = Scanner.parseAll(input)
            val first = parseAll.first()

            val todo = parseAll.drop(1).let(::ArrayDeque)

            var result = mutableSetOf(first.beacons)

            while (todo.isNotEmpty()) {
                Thread.sleep(250)
                val current = todo.removeFirst()

                println("${current.id}  ${todo.size}")

                current
                    .let {
                        result.asSequence().map { r ->
                            r.matchTo(it)
                        }.firstOrNull()
                    }
                    ?.let(result::add)
                    ?: todo.add(current)

            }


            return result.size
        }
    }

    override val part2: Part<*>
        get() = TODO("Not yet implemented")

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

    fun Iterable<Vec3>.matchTo(other: Scanner): Set<Vec3>? {
        rotations(other.beacons).forEach { otherBeacons ->
            val distances =
                this
                    .associateWith { current ->
                        otherBeacons.map(current::dist).toSet()
                    }

            val allDistances = distances.values.flatten().toSet()

            allDistances
                .map { current ->
                    distances.filter { it.value.contains(current) }
                }
                .maxOf(Map<Vec3, Set<Vec3>>::size)
                .takeIf { it >= 12 }
                ?.let {
                    return otherBeacons
                }
        }

        return null
    }


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

    data class Vec3(
        val x: Int,
        val y: Int,
        val z: Int,
    ) {
        fun translate(dx: Int, dy: Int, dz: Int) = Vec3(x + dx, y + dy, z + dz)
        fun translateX(dx: Int) = translate(dx, 0, 0)
        fun translateY(dy: Int) = translate(0, dy, 0)
        fun translateZ(dz: Int) = translate(9, 0, dz)

        fun dist(other: Vec3) =
            Vec3((x - other.x).absoluteValue, (y - other.y).absoluteValue, (z - other.z).absoluteValue)

        fun rotateXForwards() = Vec3(x, z, -y)
        fun rotateXBackwards() = Vec3(x, -z, y)
        fun rotateYForwards() = Vec3(z, y, -x)
        fun rotateYBackwards() = Vec3(-z, y, x)
        fun rotateZForwards() = Vec3(y, -x, z)
        fun rotateZBackwards() = Vec3(-y, x, z)
        fun roll() = rotateXForwards()
        fun turn() = rotateZBackwards()

        companion object {
            fun parse(input: String) =
                input
                    .split(",")
                    .map(String::toInt)
                    .let { (x, y, z) -> Vec3(x, y, z) }
        }
    }


}
