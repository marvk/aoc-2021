import java.math.BigInteger

object Day22 : Day() {
    override val part1 = object : Part<BigInteger>(BigInteger.valueOf(474140)) {
        private val range = (-50..50).let { Cuboid(it, it, it) }

        override fun solve(input: List<String>) =
            input.map(Directive.Companion::parse).filter { range.contains(it.cuboid) }.let(::Reactor).reboot()
    }

    override val part2 = object : Part<BigInteger>(BigInteger.valueOf(2758514936282235L)) {
        override fun solve(input: List<String>) =
            input.map(Directive.Companion::parse).let(::Reactor).reboot()
    }

    data class Reactor(val directives: List<Directive>) {
        private var turnedOn = listOf<Cuboid>()

        fun reboot() =
            directives.forEach(::executeDirective).let {
                turnedOn.map(Cuboid::size).reduce(BigInteger::plus)
            }

        private fun executeDirective(directive: Directive) {
            turnedOn = turnedOn.asSequence().flatMap { it.subtractDecompose(directive.cuboid) }.run {
                if (directive.newState) {
                    plus(directive.cuboid)
                } else {
                    this
                }
            }.toList()
        }
    }

    data class Directive(
        val newState: Boolean,
        val cuboid: Cuboid,
    ) {
        companion object {
            private val PATTERN =
                Regex("""(?<state>on|off) x=(?<xmin>[-]?\d+)\.\.(?<xmax>[-]?\d+),y=(?<ymin>[-]?\d+)\.\.(?<ymax>[-]?\d+),z=(?<zmin>[-]?\d+)\.\.(?<zmax>[-]?\d+)""")

            fun parse(input: String) =
                PATTERN.matchEntire(input)!!.groups.let {
                    Directive(
                        it["state"]!!.value == "on",
                        Cuboid(
                            it["xmin"]!!.value.toInt()..it["xmax"]!!.value.toInt(),
                            it["ymin"]!!.value.toInt()..it["ymax"]!!.value.toInt(),
                            it["zmin"]!!.value.toInt()..it["zmax"]!!.value.toInt(),
                        )
                    )
                }
        }
    }

    data class Cuboid(
        val xRange: IntRange,
        val yRange: IntRange,
        val zRange: IntRange,
    ) {
        val size = xRange.length() * yRange.length() * zRange.length()

        fun subtractDecompose(other: Cuboid) =
            xRange.getRanges(other.xRange).flatMap { x ->
                yRange.getRanges(other.yRange).flatMap { y ->
                    zRange.getRanges(other.zRange).map { z ->
                        Cuboid(x, y, z)
                    }
                }
            }.filterNot(other::contains).let(::merge)

        private fun merge(cuboids: List<Cuboid>) =
            cuboids.toMutableList().apply {
                while (true) {
                    mergeCandidate(this)?.run {
                        remove(first)
                        remove(second)
                        add(third)
                    } ?: break
                }
            }

        private companion object {
            private fun IntRange.concat(other: IntRange) =
                when {
                    last == other.first - 1 -> IntRange(first, other.last)
                    other.last == first - 1 -> IntRange(other.first, last)
                    else -> null
                }

            private fun mergeCandidate(cuboids: MutableList<Cuboid>) =
                cuboids.mapIndexed { index, c1 ->
                    cuboids.drop(index + 1).firstNotNullOfOrNull { c2 ->
                        val cuboid = if (c1.xMatches(c2) && c1.yMatches(c2)) {
                            c1.zRange.concat(c2.zRange)?.let { Cuboid(c1.xRange, c1.yRange, it) }
                        } else if (c1.xMatches(c2) && c1.zMatches(c2)) {
                            c1.yRange.concat(c2.yRange)?.let { Cuboid(c1.xRange, it, c1.zRange) }
                        } else if (c1.yMatches(c2) && c1.zMatches(c2)) {
                            c1.xRange.concat(c2.xRange)?.let { Cuboid(it, c1.yRange, c1.zRange) }
                        } else {
                            null
                        }

                        cuboid?.let { Triple(c1, c2, it) }
                    }
                }.filterNotNull().firstOrNull()

            private fun IntRange.length() = BigInteger.valueOf(last - first + 1L)

            private fun IntRange.contains(other: IntRange) = contains(other.first) && contains(other.last)
        }

        fun xMatches(other: Cuboid) = xRange == other.xRange
        fun yMatches(other: Cuboid) = yRange == other.yRange
        fun zMatches(other: Cuboid) = zRange == other.zRange

        fun contains(other: Cuboid) =
            xRange.contains(other.xRange) && yRange.contains(other.yRange) && zRange.contains(other.zRange)

        private fun IntRange.getRanges(otherRange: IntRange) =
            buildList {
                if (otherRange.contains(this@getRanges) || last < otherRange.first || first > otherRange.last) {
                    add(this@getRanges)
                } else if (first >= otherRange.first && last > otherRange.last) {
                    add(first..otherRange.last)
                    add(otherRange.last + 1..last)
                } else if (first < otherRange.first && last <= otherRange.last) {
                    add(first..otherRange.first - 1)
                    add(otherRange.first..last)
                } else if (first < otherRange.first && last > otherRange.last) {
                    add(first..otherRange.first - 1)
                    add(otherRange)
                    add(otherRange.last + 1..last)
                } else {
                    throw AssertionError("I hope this is unreachable")
                }
            }
    }
}