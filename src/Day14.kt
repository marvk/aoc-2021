object Day14 : Day() {
    override val part1 = object : Part<Long>(1588L) {
        override fun solve(input: List<String>) =
            input.let(LightweightPolymerInstructions::parse).applyRulesNTimes(10).result()
    }

    override val part2 = object : Part<Long>(2188189693529L) {
        override fun solve(input: List<String>) =
            input.let(LightweightPolymerInstructions::parse).applyRulesNTimes(40).result()
    }

    private data class LightweightPolymerInstructions(
        private val polymers: Map<Rule, Long>,
        private val endsIn: Char,
        private val rules: Map<Rule, List<Rule>>,
    ) {
        fun result() =
            polymers
                .entries
                .map { it.key.pair.first() to it.value }
                .plus(endsIn to 1L)
                .groupingBy { it.first }
                .fold(0L) { accumulator, charToCount ->
                    accumulator + charToCount.second
                }
                .values
                .run {
                    maxOf { it } - minOf { it }
                }


        fun applyRulesNTimes(n: Int) =
            generateSequence(this, LightweightPolymerInstructions::applyRules)
                .take(n + 1)
                .last()

        fun applyRules(): LightweightPolymerInstructions =
            polymers
                .flatMap(::resolveEntry)
                .groupingBy(Pair<Rule, Long>::first)
                .fold(0L) { accumulator, ruleToCount ->
                    accumulator + ruleToCount.second
                }
                .let {
                    LightweightPolymerInstructions(it, endsIn, rules)
                }

        private fun resolveEntry(entry: Map.Entry<Rule, Long>) =
            rules[entry.key]!!.map { it to entry.value }

        companion object {
            fun parse(input: List<String>) =
                partitionInput(input)
                    .let { (template, rules) ->
                        LightweightPolymerInstructions(
                            templateToRules(template, rules),
                            template.last(),
                            ruleResultsByRule(rules)
                        )
                    }

            private fun ruleResultsByRule(rules: Map<String, Rule>) =
                rules.run {
                    values.associateWith { rule ->
                        rule.results.map {
                            this[it]!!
                        }
                    }
                }

            private fun templateToRules(template: String, rules: Map<String, Rule>) =
                template
                    .windowed(2)
                    .map {
                        rules[it]!!
                    }
                    .groupingBy { it }
                    .eachCount()
                    .mapValues { it.value.toLong() }
                    .toMap()

            private fun partitionInput(input: List<String>) =
                input
                    .filter(String::isNotEmpty)
                    .partition { it.contains("->") }
                    .let {
                        it.second.single() to it.first.map(Rule::parse).associateBy(Rule::pair)
                    }
        }
    }

    private data class Rule(
        val pair: String,
        val results: List<String>,
    ) {
        companion object {
            fun parse(input: String) =
                input
                    .split(" -> ")
                    .let { (pair, inserted) ->
                        Rule(
                            pair,
                            listOf(
                                pair[0] + inserted,
                                inserted + pair[1]
                            )
                        )
                    }
        }
    }
}