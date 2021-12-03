object Day03 : Day<Int>() {

    override val part1 = part(198) { input ->
        input
            .fold(part1Accumulator(input)) { acc, row ->
                acc.apply {
                    row.forEachIndexed { index, s ->
                        this[index] += if (s == '1') 1 else 0
                    }
                }
            }
            .reversedArray()
            .foldIndexed(Rates(0, 0)) { index, rates, ones ->
                val currentBit = if (ones > (input.size / 2)) 1 else 0
                Rates(
                    rates.gamma.or(currentBit.shl(index)),
                    rates.epsilon.or(currentBit.xor(1).shl(index))
                )
            }
            .let { it.gamma * it.epsilon }
    }

    private fun part1Accumulator(input: List<String>) =
        input
            .firstOrNull()
            ?.let { IntArray(it.length) }
            ?: throw IllegalArgumentException("Input must not be empty")

    override val part2 = part(230) { input ->
        val oxygen = input.toMutableList()
        val co2 = input.toMutableList()

        for (index in 0 until input.first().length) {
            cull(index, oxygen, '1')
            cull(index, co2, '0')
        }

        return@part oxygen.first().toInt(2) * co2.first().toInt(2)
    }

    private fun Char.opposite() =
        when (this) {
            '1' -> '0'
            '0' -> '1'
            else -> throw IllegalArgumentException()
        }

    private fun cull(index: Int, diagnostics: MutableList<String>, charToKeepIfOnesDominate: Char) {
        if (diagnostics.size <= 1) {
            return
        }

        val ones = diagnostics
            .map { it[index] }
            .count { it == '1' }

        val keepChar = when {
            ones * 2 >= diagnostics.size -> charToKeepIfOnesDominate
            else -> charToKeepIfOnesDominate.opposite()
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