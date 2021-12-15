import kotlin.math.absoluteValue

object Day15 : Day() {
    override val part1 = object : Part<Int>(40) {
        override fun solve(input: List<String>) =
            input
                .let(Graph::parse)
                .shortestPath()
                .drop(1)
                .sumOf(Node::value)
    }

    override val part2 = object : Part<Int>(315) {
        override fun solve(input: List<String>) =
            input
                .let { Graph.parse(it, expandBy = 5) }
                .shortestPath()
                .drop(1)
                .sumOf(Node::value)
    }

    private data class Graph(
        private val map: List<List<Node>>,
    ) {
        private val width = map[0].size
        private val height = map.size
        private val start = get(0, 0)!!
        private val end = get(width - 1, height - 1)!!

        fun shortestPath(): List<Node> {
            fun h(node: Node) = node.distanceToEnd()
            fun d(node1: Node, node2: Node) = node1.value + node2.value

            val openSet = mutableSetOf(start)
            val cameFrom = mutableMapOf<Node, Node>()
            val gScore = map.flatten().associateWith { Int.MAX_VALUE }.toMutableMap().apply { put(start, 0) }
            val fScore = gScore.toMutableMap().apply { put(start, h(start)) }

            while (openSet.isNotEmpty()) {
                val current = openSet.asSequence().sortedBy { fScore[it] }.first()

                if (current == end) {
                    return reconstructPath(cameFrom, current)
                }

                openSet.remove(current)

                for (neighbour in current.neighbours()) {
                    val provisionalGScore = gScore[current]!! + d(current, neighbour)

                    if (provisionalGScore < gScore[neighbour]!!) {
                        cameFrom[neighbour] = current
                        gScore[neighbour] = provisionalGScore
                        fScore[neighbour] = provisionalGScore + h(neighbour)
                        openSet.add(neighbour)
                    }
                }
            }

            throw IllegalStateException("no path found")
        }

        private fun reconstructPath(cameFrom: Map<Node, Node>, last: Node): List<Node> {
            val path = mutableListOf(last)
            var current = last
            while (true) {
                current = cameFrom[current] ?: return path
                path.add(0, current)
            }
        }

        private fun get(x: Int, y: Int) = map.getOrNull(y)?.getOrNull(x)

        private fun Node.distanceToEnd() = distanceTo(end)

        private fun Node.neighbours() =
            listOfNotNull(
                get(x + 1, y),
                get(x - 1, y),
                get(x, y + 1),
                get(x, y - 1),
            )

        companion object {
            fun parse(input: List<String>, expandBy: Int = 1) =
                input
                    .map { it.map(Char::digitToInt) }
                    .repeat(expandBy) { index, rows ->
                        rows.map { incrementByIndex(index, it) }
                    }
                    .mapIndexed { y, line -> parseLine(y, line, expandBy) }
                    .let(::Graph)

            private fun parseLine(y: Int, line: List<Int>, expandBy: Int = 1) =
                line
                    .repeat(expandBy, ::incrementByIndex)
                    .mapIndexed { x, value ->
                        Node(x, y, value)
                    }

            private fun <T> Iterable<T>.repeat(times: Int, mapping: (index: Int, Iterable<T>) -> List<T>): List<T> {
                return Array(times) { this }
                    .mapIndexed { index, iterable -> mapping(index, iterable) }
                    .flatten()
            }

            private fun incrementByIndex(index: Int, ints: Iterable<Int>) =
                ints.map { 1 + (it + index - 1) % 9 }
        }
    }

    private data class Node(
        val x: Int,
        val y: Int,
        val value: Int,
    ) {
        fun distanceTo(to: Node?) =
            to?.let { manhattanDistance(this.x, this.y, it.x, it.y) } ?: Int.MAX_VALUE

        private fun manhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int) =
            x1.minus(x2).absoluteValue + y1.minus(y2).absoluteValue
    }
}