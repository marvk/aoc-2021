object Day10 : Day() {
    override val part1 = object : Part<Long>(26397L) {
        override fun solve(input: List<String>) =
            input
                .asSequence()
                .map(::solveLine)
                .filterIsInstance<Result.Illegal>()
                .sumOf(Result.Illegal::points)
    }

    override val part2 = object : Part<Long>(288957L) {
        override fun solve(input: List<String>) =
            input
                .asSequence()
                .map(::solveLine)
                .filterIsInstance<Result.Incomplete>()
                .map(Result.Incomplete::points)
                .sorted()
                .toList()
                .let {
                    it[it.size / 2]
                }
    }

    private fun solveLine(string: String): Result {
        val history = mutableListOf<Bracket>()

        string.chunked(1).forEach { bracketString ->
            Bracket
                .getOpeningOrNull(bracketString)
                ?.also { history += it }
                ?: Bracket.getClosingOrNull(bracketString)!!
                    .let {
                        when {
                            history.isEmpty() -> Unit
                            history.last() == it -> history.removeLast()
                            else -> return Result.Illegal(it.illegalPoints)
                        }
                    }
        }

        return if (history.isEmpty()) Result.Valid else
            history
                .reversed()
                .map(Bracket::incompletePoints)
                .fold(0L) { acc, i -> acc * 5L + i }
                .let(Result::Incomplete)
    }

    private sealed interface Result {
        object Valid : Result
        data class Incomplete(val points: Long) : Result
        data class Illegal(val points: Long) : Result
    }

    private enum class Bracket(
        val opening: String,
        val closing: String,
        val illegalPoints: Long,
        val incompletePoints: Long,
    ) {
        ROUND("(", ")", 3, 1),
        SQUARE("[", "]", 57, 2),
        CURLY("{", "}", 1197, 3),
        ANGLE("<", ">", 25137, 4);

        companion object {
            fun getOpeningOrNull(string: String) =
                values().singleOrNull { it.opening == string }

            fun getClosingOrNull(string: String) =
                values().singleOrNull { it.closing == string }
        }
    }
}