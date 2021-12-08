object Day08 : Day<Int>() {
    override val part1 = object : Part(26) {
        override fun solve(input: List<String>) =
            parseRows(input)
                .sumOf(InputRow::countKnownValuesInOutput)
    }

    override val part2 = object : Part(61229) {
        override fun solve(input: List<String>) =
            parseRows(input)
                .sumOf(InputRow::decodeOutput)
    }

    private fun parseRows(input: List<String>) =
        input
            .asSequence()
            .map { it.split(" | ") }
            .map(::parseSections)
            .map { (left, right) -> InputRow(left, right) }

    private fun parseSections(sections: Iterable<String>) =
        sections
            .map { it.split(" ") }
            .map { it.map(::Code) }
            .zipWithNext()
            .single()

    private data class Code(val chars: Set<Char>) {
        val length = chars.size

        constructor(string: String) : this(string.toCharArray().toSet())

        infix fun contains(other: Set<Char>) = chars.containsAll(other)

        infix fun contains(other: Code) = contains(other.chars)

        operator fun minus(other: Code) = chars.minus(other.chars)

        companion object {
            val BLANK = Code("")
        }
    }

    private data class CharacterCodes(val codes: List<Code>) {
        operator fun get(code: Code): Int = codes.indexOf(code)
    }

    private data class InputRow(
        val signalPattern: List<Code>,
        val outputValue: List<Code>,
    ) {
        private val characterCodes: CharacterCodes by lazy(::solveCharacterCodes)

        private val knownValues = UniqueLengthCodes.parseInput(signalPattern)

        fun decodeOutput() =
            outputValue
                .map { characterCodes[it] }
                .joinToString("")
                .toInt()

        fun countKnownValuesInOutput() =
            outputValue.count(knownValues.values::contains)

        private fun signalsWithLength(length: Int) = signalPattern.filter { it.length == length }

        private fun solveCharacterCodes(): CharacterCodes {
            val result = Array(10) { Code.BLANK }

            with(knownValues) {
                result[1] = one
                result[4] = four
                result[7] = seven
                result[8] = eight
            }

            val (six, lengthSixRemaining) = signalsWithLength(6).partition { it contains result[8] - result[7] }
            result[6] = six.single()
            val (nine, zero) = lengthSixRemaining.partition { it contains result[4] }
            result[0] = zero.single()
            result[9] = nine.single()

            val (three, lengthFiveRemaining) = signalsWithLength(5).partition { it contains result[7] }
            result[3] = three.single()
            val (five, two) = lengthFiveRemaining.partition { result[9] contains it }
            result[5] = five.single()
            result[2] = two.single()

            return result.toList().let(::CharacterCodes)
        }
    }

    private data class UniqueLengthCodes(
        val one: Code,
        val four: Code,
        val seven: Code,
        val eight: Code,
    ) {
        val values = listOf(one, four, seven, eight)

        companion object {
            fun parseInput(testDigits: List<Code>) = UniqueLengthCodes(
                getByLength(testDigits, 2),
                getByLength(testDigits, 4),
                getByLength(testDigits, 3),
                getByLength(testDigits, 7),
            )

            private fun getByLength(signalPattern: List<Code>, length: Int): Code {
                return signalPattern.single { it.length == length }
            }
        }
    }
}