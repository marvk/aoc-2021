object Day12 : Day() {
    override val part1 = object : Part<Int>(226) {
        override fun solve(input: List<String>): Int {
            return Graph.parse(input).pathsThatVisitSmallCavesAtMostOnce().size
        }
    }

    override val part2 = object : Part<Int>(3509) {
        override fun solve(input: List<String>): Int {
            return Graph.parse(input).pathsThatVisitOneSmallCaveAtMoseTwiceElseAtMostOnce().size
        }
    }

    private data class Path(val nodes: List<Node>)

    private data class Graph(val nodes: Set<Node>, val edges: Set<Edge>) {
        private val start = nodes.single { it.type == Node.Type.START }
        private val end = nodes.single { it.type == Node.Type.END }

        private val neighbours =
            edges
                .plus(edges.map { (from, to) -> Edge(to, from) })
                .groupBy(Edge::from)
                .mapValues { it.value.map(Edge::to) }

        private fun neighbours(node: Node) = neighbours[node]!!

        fun pathsThatVisitSmallCavesAtMostOnce() =
            traverse(start, listOf(start)) { currentPath ->
                currentPath
                    .only(Node.Type.START, Node.Type.SMALL)
                    .toSet()
            }

        fun pathsThatVisitOneSmallCaveAtMoseTwiceElseAtMostOnce() =
            traverse(start, listOf(start)) { currentPath ->
                currentPath
                    .only(Node.Type.SMALL)
                    .let {
                        when (it.size) {
                            it.toSet().size -> currentPath.only(Node.Type.START)
                            else -> currentPath.only(Node.Type.START, Node.Type.SMALL)
                        }
                    }
                    .toSet()
            }

        private fun List<Node>.only(vararg types: Node.Type) =
            this.filter { types.contains(it.type) }

        private fun traverse(
            currentNode: Node,
            currentPath: List<Node>,
            findUnvisitableCaves: (currentPath: List<Node>) -> Set<Node>,
        ): Set<Path> =
            when (currentNode) {
                end -> setOf(Path(currentPath))
                else -> neighbours(currentNode)
                    .minus(findUnvisitableCaves(currentPath))
                    .flatMap { neighbour -> traverse(neighbour, currentPath + neighbour, findUnvisitableCaves) }
                    .toSet()
            }

        companion object {
            fun parse(input: List<String>) =
                input
                    .asSequence()
                    .map { it.split("-").map(::Node) }
                    .map { (from, to) -> Edge(from, to) }
                    .toSet()
                    .let {
                        Graph(
                            it.asSequence().flatMap(Edge::nodes).toSet(),
                            it
                        )
                    }
        }
    }


    private data class Edge(val from: Node, val to: Node) {
        val nodes = setOf(from, to)
    }

    private data class Node(val name: String) {
        val type: Type = Type.fromString(name)

        enum class Type {
            BIG, SMALL, START, END;

            companion object {
                fun fromString(s: String) = when {
                    s.isEmpty() -> throw IllegalArgumentException("s must not be empty")
                    s == "start" -> START
                    s == "end" -> END
                    s.lowercase() == s -> SMALL
                    else -> BIG
                }
            }
        }
    }

}