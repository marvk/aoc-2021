object Day18 : Day() {
    override val part1 = object : Part<Long>(4140) {
        override fun solve(input: List<String>): Long {
            return input.map(Node.Companion::parse).reduce(Node::plus).magnitude
        }
    }

    override val part2 = object : Part<Long>(3993) {
        override fun solve(input: List<String>): Long {
            val nodes = input.map(Node.Companion::parse)

            return nodes
                .flatMap { a ->
                    nodes.map { b -> a to b }
                }
                .flatMap {
                    listOf(it, it.second to it.first)
                }
                .onEach { println("${it.first}\n${it.second}\n\n") }
                .maxByOrNull { it.first.plus(it.second).magnitude }!!
                .also { println("nani\n${it.first}\n${it.second}") }
                .let { it.first.plus(it.second).magnitude }
        }

    }


    sealed interface Node {
        val magnitude: Long

        data class Inner(var left: Node, var right: Node) : Node {
            override val magnitude
                get() = left.magnitude * 3 + right.magnitude * 2

            override fun toPrettyString() =
                "[ ($magnitude) \n${left.toPrettyString().prependIndent("    ")}\n${
                    right.toPrettyString().prependIndent("    ")
                }\n]"

            override fun toString() =
                "[$left,$right]"

            fun leafs() =
                left as Leaf to right as Leaf

            override fun copy(): Node {
                return Inner(left.copy(), right.copy())
            }
        }

        fun toPrettyString(): String

        class Leaf(var value: Int) : Node {
            override val magnitude
                get() = value.toLong()

            override fun toPrettyString() =
                value.toString()

            override fun toString() =
                value.toString()

            override fun copy(): Node {
                return Leaf(value)
            }
        }

        fun plus(other: Node): Node {
            return Inner(this.copy(), other.copy()).apply { reduce() }
        }

        private sealed interface ReductionAction {
            data class ExplodedLeft(val value: Int, val debug: String = "") : ReductionAction {
                fun applyToNode(node: Node) {
                    when (node) {
                        is Leaf -> node.value += value
                        is Inner -> applyToNode(node.right)
                    }
                }
            }

            data class ExplodedRight(val value: Int, val debug: String = "") : ReductionAction {
                fun applyToNode(node: Node) {
                    when (node) {
                        is Leaf -> node.value += value
                        is Inner -> applyToNode(node.left)
                    }
                }
            }

            data class ReductionCompleted(val debug: String = "") : ReductionAction
        }

        private fun Inner.reduce(): Node {
            do {
                val reduce = explode(1) ?: split()
            } while (reduce != null)
            return this
        }

        fun pretty() {
            println("~".repeat(100))
            println(this.toPrettyString())
        }

        private fun Inner.split(): ReductionAction? {
            if (left is Leaf) {
                val before = (left as Leaf).copy()
                reduceLeaf(left as Leaf)
                    ?.also {
                        left = it
                    }?.also {
                        return ReductionAction.ReductionCompleted("left leaf split {$before -> $left}")
                    }
            } else {
                (left as Inner).split()?.run { return this }
            }

            if (right is Leaf) {
                val before = (right as Leaf).copy()
                reduceLeaf(right as Leaf)
                    ?.also {
                        right = it
                    }?.also {
                        return ReductionAction.ReductionCompleted("right leaf split {$before -> $right}")
                    }
            } else {
                (right as Inner).split()?.run { return this }
            }

            return null
        }

        private fun Inner.explode(depth: Int): ReductionAction? {
            (left as? Inner)?.let {
                if (depth >= 4) {
                    val before = (left as Inner).copy()
                    val (leftLeaf, rightLeaf) = (left as Inner).leafs()
                    left = Leaf(0)
                    ReductionAction.ExplodedRight(rightLeaf.value).applyToNode(right)
                    return ReductionAction.ExplodedLeft(leftLeaf.value,
                        "left leaf exploded {$before -> $leftLeaf 0 $rightLeaf}")
                } else {
                    when (val action = (left as Inner).explode(depth + 1)) {
                        is ReductionAction.ExplodedRight -> {
                            action.applyToNode(right)
                            return ReductionAction.ReductionCompleted(action.debug)
                        }
                        is ReductionAction.ExplodedLeft -> return action
                        is ReductionAction.ReductionCompleted -> return action
                        else -> {}
                    }
                }
            }

            (right as? Inner)?.let {
                if (depth >= 4) {
                    val before = (right as Inner).copy()
                    val (leftLeaf, rightLeaf) = (right as Inner).leafs()
                    right = Leaf(0)
                    ReductionAction.ExplodedLeft(leftLeaf.value).applyToNode(left)
                    return ReductionAction.ExplodedRight(rightLeaf.value,
                        "right leaf exploded {$before -> $leftLeaf 0 $rightLeaf}")
                } else {
                    when (val action = (right as Inner).explode(depth + 1)) {
                        is ReductionAction.ExplodedLeft -> {
                            action.applyToNode(left)
                            return ReductionAction.ReductionCompleted(action.debug)
                        }
                        is ReductionAction.ExplodedRight -> return action
                        is ReductionAction.ReductionCompleted -> return action
                        null -> {}
                    }
                }
            }

            return null
        }

        fun reduceLeaf(leaf: Leaf): Inner? {
            val leftValue = leaf.value
            if (leftValue > 9) {
                val a = leftValue / 2
                val b = leftValue / 2 + leftValue % 2
                return Inner(Leaf(a), Leaf(b))
            }
            return null
        }

        abstract fun copy(): Node

        companion object {
            fun parse(input: String) = LexerParser(input).parse()

            fun parse(input: List<String>): List<Node> =
                input.map(::parse)
        }
    }

    class LexerParser(input: String) {
        private val input: MutableList<Char> = input.toMutableList()
        private val currentChar
            get() = input[0]

        private var consumed = false

        fun parse() = when (consumed) {
            true -> throw IllegalStateException()
            else -> parseNode().also { consumed = true }
        }

        private fun parseNode(): Node =
            consume('[')
                .let { parseValue() }
                .also { consume(',') }
                .let { it to parseValue() }
                .also { consume(']') }
                .let { Node.Inner(it.first, it.second) }


        private fun parseValue() =
            when {
                currentChar.isDigit() -> Node.Leaf(consume().digitToInt())
                else -> parseNode()
            }

        private fun consume(): Char {
            return input.removeAt(0)
        }

        private fun consume(char: Char): Char {
            return input.removeAt(0).takeIf { it == char } ?: throw IllegalStateException()
        }
    }
}