object Day03 : Day<Int>() {

    override val part1 = part(198) { input ->
        input
            .map(String::toCharArray)
            .fold(part1Accumulator(input)) { acc, strings ->
                acc.apply {
                    strings.forEachIndexed { index, s ->
                        this[index] += if (s == '1') 1 else 0
                    }
                }
            }
            .let { intArrayToBinaryNumbers(input.size, it) }
            .let { it.first * it.second }
    }


    private fun intArrayToBinaryNumbers(size: Int, array: IntArray): Pair<Int, Int> {
        var first = 0
        var second = 0

        array.reversedArray().forEachIndexed { index, i ->
            val currentBit = if (i > (size / 2)) 1 else 0
            first = first.or(currentBit.shl(index))
            second = second.or(currentBit.xor(1).shl(index))
        }

        array
            .reversedArray()
            .foldIndexed(Rates(0, 0)) { index, acc, i ->
                Rates(acc.gamma, acc.epsilon)
            }

        return Pair(first, second)
    }

    private fun part1Accumulator(input: List<String>) =
        input
            .firstOrNull()
            ?.let { IntArray(it.length) }
            ?: throw IllegalArgumentException("Input must not be empty")

    override val part2 = part(230) { input ->
        val oxygen = input.toMutableList()
        val co2 = input.toMutableList()

        check(input.isNotEmpty())

        for (i in 0 until input.first().length) {
            cull(i, oxygen, '1')
            cull(i, co2, '0')
        }

        check(oxygen.size == 1)
        check(co2.size == 1)

        return@part oxygen.first().toInt(2) * co2.first().toInt(2)
    }

    private fun Char.opposite() =
        when (this) {
            '1' -> '0'
            '0' -> '1'
            else -> throw IllegalArgumentException()
        }

    private fun cull(index: Int, diagnostics: MutableList<String>, keepCharIfOnesDominate: Char) {
        if (diagnostics.size <= 1) {
            return
        }

        val ones = diagnostics
            .map { it[index] }
            .count { it == '1' }

        val keepChar = when {
            ones * 2 >= diagnostics.size -> keepCharIfOnesDominate
            else -> keepCharIfOnesDominate.opposite()
        }

        diagnostics
            .removeIf {
                it[index] != keepChar
            }
    }

    private data class Rates(
        val gamma: Int,
        val epsilon: Int,
    )
}