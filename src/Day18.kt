import Day18.Node.ReductionAction.*

object Day18 : Day() {
    override val part1 = object : Part<Long>(4140) {
        override fun solve(input: List<String>) =
            input
                .map(Node.Companion::parse)
                .reduce(Node::plus)
                .magnitude
    }

    override val part2 = object : Part<Long>(3993) {
        override fun solve(input: List<String>) =
            input
                .map(Node.Companion::parse)
                .let {
                    it.flatMap { a ->
                        it.map { b -> a to b }
                    }
                }
                .flatMap {
                    listOf(it, it.second to it.first)
                }
                .map { it.first + it.second }
                .maxOf(Node::magnitude)
    }

    sealed interface Node {
        val magnitude: Long

        data class Inner(var left: Node, var right: Node) : Node {
            override val magnitude
                get() = left.magnitude * 3 + right.magnitude * 2

            fun leafs() = left as Leaf to right as Leaf
            override fun deepCopy() = Inner(left.deepCopy(), right.deepCopy())
            override fun toString() = "[$left,$right]"
        }

        data class Leaf(var value: Int) : Node {
            override val magnitude
                get() = value.toLong()

            override fun deepCopy() = copy()
            override fun toString() = value.toString()
        }

        operator fun plus(other: Node) =
            Inner(this, other).deepCopy().also { it.reduce() }


        private fun Inner.reduce() =
            generateSequence { reduceExplode(1) ?: reduceSplit() }.lastOrNull().let { this }

        private fun Inner.reduceSplit(): ReductionAction? {
            (left as? Leaf)
                ?.split()
                ?.also { left = it }
                ?.also { return ReductionCompleted }

            (left as? Inner)
                ?.reduceSplit()
                ?.also { return it }

            (right as? Leaf)
                ?.split()
                ?.also { right = it }
                ?.also { return ReductionCompleted }

            (right as? Inner)
                ?.reduceSplit()
                ?.also { return it }

            return null
        }

        private fun Leaf.split() =
            value
                .takeIf { it > 9 }
                ?.let(::generateSplitInnerFromValue)

        private fun generateSplitInnerFromValue(value: Int) =
            Inner(Leaf(value / 2), Leaf(value / 2 + value % 2))

        private fun Inner.explodeLeafs() =
            listOf(left as Leaf, right as Leaf)
                .map { it.value }
                .let { (l, r) -> ExplodedLeft(l) to ExplodedRight(r) }

        private fun Inner.reduceExplode(depth: Int): ReductionAction? {
            (left as? Inner)?.let { leftAsInner ->
                if (depth >= 4) {
                    return leftAsInner
                        .explodeLeafs()
                        .also { left = Leaf(0) }
                        .also { (_, r) -> r.applyToNode(right) }
                        .let { (l, _) -> l }
                } else {
                    when (val action = leftAsInner.reduceExplode(depth + 1)) {
                        is ExplodedRight -> ReductionCompleted.also { action.applyToNode(right) }
                        is ExplodedLeft, ReductionCompleted -> action
                        else -> null
                    }?.let { return it }
                }
            }

            (right as? Inner)?.let { rightAsInner ->
                if (depth >= 4) {
                    return rightAsInner
                        .explodeLeafs()
                        .also { right = Leaf(0) }
                        .also { (l, _) -> l.applyToNode(left) }
                        .let { (_, r) -> r }
                } else {
                    when (val action = rightAsInner.reduceExplode(depth + 1)) {
                        is ExplodedLeft -> ReductionCompleted.also { action.applyToNode(left) }
                        is ExplodedRight, ReductionCompleted -> return action
                        else -> null
                    }?.let { return it }
                }
            }

            return null
        }

        fun deepCopy(): Node

        private sealed interface ReductionAction {
            data class ExplodedLeft(val value: Int) : ReductionAction {
                fun applyToNode(node: Node) {
                    when (node) {
                        is Leaf -> node.value += value
                        is Inner -> applyToNode(node.right)
                    }
                }
            }

            data class ExplodedRight(val value: Int) : ReductionAction {
                fun applyToNode(node: Node) {
                    when (node) {
                        is Leaf -> node.value += value
                        is Inner -> applyToNode(node.left)
                    }
                }
            }

            object ReductionCompleted : ReductionAction
        }

        companion object {
            fun parse(input: String) = LexerParser(input).parse()

            fun parse(input: List<String>): List<Node> =
                input.map(::parse)

            private class LexerParser(input: String) {
                private val input: MutableList<Char> = input.toMutableList()
                private val currentChar
                    get() = input[0]

                private var consumed = false

                fun parse() = when (consumed) {
                    true -> throw IllegalStateException()
                    false -> parseNode().also { consumed = true }
                }

                private fun parseNode(): Node =
                    consume('[')
                        .run { parseValue() }
                        .also { consume(',') }
                        .let { it to parseValue() }
                        .also { consume(']') }
                        .let { Node.Inner(it.first, it.second) }


                private fun parseValue() =
                    when {
                        currentChar.isDigit() -> Leaf(consume().digitToInt())
                        else -> parseNode()
                    }

                private fun consume() = input.removeAt(0)

                private fun consume(char: Char) =
                    input.removeAt(0).takeIf { it == char } ?: throw IllegalStateException()
            }
        }
    }
}