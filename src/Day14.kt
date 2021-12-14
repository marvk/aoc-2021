object Day14 : Day() {
    override val part1 = object : Part<Long>(1588L) {
        override fun solve(input: List<String>) =
            input.let(PolymerInstructions::parse).applyRulesNTimes(10).part1Output()
    }

    override val part2 = object : Part<Long>(2188189693529L) {
        override fun solve(input: List<String>): Long {
            return input.let(PolymerInstructions::parse).applyRulesNTimes(40).part1Output()
        }
    }

    private data class PolymerInstructions(
        val template: String,
        val insertionRules: List<InsertionRule>,
    ) {
        private val insertionRulesByPair = insertionRules.associateBy(InsertionRule::pair)

        fun part1Output() =
            template
                .groupingBy { it }
                .eachCount()
                .values
                .let { values ->
                    values.maxOf { it } - values.minOf { it }
                }
                .toLong()

        fun applyRulesNTimes(n: Int) =
            generateSequence(this, PolymerInstructions::applyRules)
                .onEachIndexed { index, _ -> println(index) }
                .take(n + 1)
                .last()

        fun applyRules(): PolymerInstructions {
            return PolymerInstructions(
                template
                    .windowed(2)
                    .joinToString("", postfix = template.last().toString()) {
                        insertionRulesByPair[it]!!.result
                    },
                insertionRules
            )
        }

        companion object {
            fun parse(input: List<String>) =
                input
                    .filter(String::isNotEmpty)
                    .partition { it.contains("->") }
                    .let { (insertionRulesString, template) ->
                        PolymerInstructions(
                            template.single(),
                            insertionRulesString.map(InsertionRule::parse)
                        )
                    }
        }
    }

    private data class InsertionRule(
        val pair: String,
        val inserted: String,
    ) {
        val result = pair[0] + inserted

        companion object {
            fun parse(input: String) =
                input
                    .split(" -> ")
                    .let { (pair, inserted) ->
                        InsertionRule(
                            pair,
                            inserted
                        )
                    }
        }
    }
}