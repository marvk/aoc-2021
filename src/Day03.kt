object Day03 : Day() {
    override val part1 = object : Part<Int>(198) {
        override fun solve(input: List<String>) =
            input
                .fold(input.first().length.let(::IntArray)) { acc, row ->
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

    override val part2 = object : Part<Int>(230) {
        override fun solve(input: List<String>): Int {
            val oxygen = input.toMutableList()
            val co2 = input.toMutableList()

            for (index in 0 until input.first().length) {
                cull(index, oxygen, '1')
                cull(index, co2, '0')
            }

            return oxygen.first().toInt(2) * co2.first().toInt(2)
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
    }

    private data class Rates(
        val gamma: Int,
        val epsilon: Int,
    )
}