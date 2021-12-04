object Day04 : Day<Int>() {
    private val wonRowOrColumn = List(5) { -1 }

    override val part1 = object : Part(4512) {
        override fun solve(input: List<String>): Int {
            val (numbers, bingoCards) = splitAndParseInput(input)

            for (number in numbers) {
                for (bingoCard in bingoCards) {
                    bingoCard.mark(number)

                    if (bingoCard.won) {
                        return bingoCard.sumOfRemaining() * number
                    }
                }
            }

            throw IllegalStateException()
        }
    }

    override val part2 = object : Part(1924) {
        override fun solve(input: List<String>): Int {
            val (numbers, bingoCards) = splitAndParseInput(input)

            var bingoCardsNotWon = bingoCards

            for (number in numbers) {
                for (bingoCard in bingoCardsNotWon) {
                    bingoCard.mark(number)
                }

                val (won, notWon) = bingoCardsNotWon.partition(BingoCard::won)

                won.singleOrNull()?.let { lastWonCard ->
                    if (notWon.isEmpty()) {
                        return lastWonCard.sumOfRemaining() * number
                    }
                }

                bingoCardsNotWon = notWon
            }

            throw IllegalStateException()
        }
    }

    private fun splitAndParseInput(input: List<String>): Pair<List<Int>, List<BingoCard>> {
        val numbers = input.first().split(",").map(String::toInt)
        val bingoCards = input
            .asSequence()
            .drop(2)
            .windowed(5, 6)
            .map { it.map(::parseRow) }
            .map(::BingoCard)
            .toList()
        return Pair(numbers, bingoCards)
    }

    private fun parseRow(it: String) = it
        .trim()
        .split(Regex("""\s+"""))
        .map(String::toInt)
        .toMutableList()

    private data class BingoCard(
        private val rows: List<MutableList<Int>>,
    ) {
        private val n = rows[0].size

        private val cols =
            (0 until n).map { col ->
                (0 until n).map { row ->
                    rows[row][col]
                }.toMutableList()
            }

        var won = false
            private set

        fun mark(number: Int) {
            if (won) {
                return
            }

            for (row in 0 until n) {
                for (col in 0 until n) {
                    if (rows[row][col] == number) {
                        rows[row][col] = -1
                        cols[col][row] = -1

                        if (rows[row] == wonRowOrColumn || cols[col] == wonRowOrColumn) {
                            won = true
                        }
                    }
                }
            }
        }

        fun sumOfRemaining() =
            rows.flatten().filter { it >= 0 }.sum()
    }
}