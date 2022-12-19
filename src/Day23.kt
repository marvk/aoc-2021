import Day23.Amphipod.AMBER
import Day23.Amphipod.BRONZE
import Day23.Amphipod.COPPER
import Day23.Amphipod.DESERT
import java.util.BitSet

object Day23 : Day() {
    override val part1 = object : Part<Long>(12521) {
        override fun solve(input: List<String>) =
            input
                .toMutableList()
                .apply {
                    add(4, "#A#B#C#D#")
                    add(5, "#A#B#C#D#")
                }
                .let(Burrow.Companion::parse)
                .solveRec()!!
    }

    override val part2 = object : Part<Long>(44169) {
        override fun solve(input: List<String>) =
            input
                .toMutableList()
                .apply {
                    add(3, "#D#C#B#A#")
                    add(4, "#D#B#A#C#")
                }
                .let(Burrow.Companion::parse)
                .solveRec()!!
    }

    enum class Amphipod(val shiftTimes: Int, val char: Char, val cost: Long, val homeNodes: List<Int>) {
        AMBER(0, 'A', 1, listOf(7, 11, 15, 19)),
        BRONZE(1, 'B', 10, listOf(8, 12, 16, 20)),
        COPPER(2, 'C', 100, listOf(9, 13, 17, 21)),
        DESERT(3, 'D', 1000, listOf(10, 14, 18, 22)),
    }

    class Burrow(raw: List<Amphipod?>) {
        override fun toString() =
            (0..22)
                .map(::get)
                .map { it?.char ?: '.' }
                .toTypedArray()
                .let {
                    """
                    #############
                    #%s%s.%s.%s.%s.%s%s#
                    ###%s#%s#%s#%s###
                      #%s#%s#%s#%s#
                      #%s#%s#%s#%s#
                      #%s#%s#%s#%s#
                      #########
                    """.trimIndent().format(*it)
                }

        private var bitMap = encode(raw);

        private val hashTable: MutableMap<BitSet, HashHit> = mutableMapOf();

        private fun get(node: Node) = get(node.id)
        private fun get(id: Int): Amphipod? {
            if (id > 22) {
                return null
            } else {
                return if (bitMap.get(id)) {
                    AMBER
                } else if (bitMap.get(id + 23)) {
                    BRONZE
                } else if (bitMap.get(id + 23 * 2)) {
                    COPPER
                } else if (bitMap.get(id + 23 * 3)) {
                    DESERT
                } else {
                    null
                }
            }
        }

        private fun isEmpty(node: Node) = isEmpty(node.id)
        private fun isEmpty(id: Int) = get(id) == null

        private fun move(from: Node, to: Node) {
            val amphipod = get(from.id) ?: throw java.lang.IllegalStateException("\n$this");
            bitMap.clear(from.id)
            bitMap.clear(from.id + 23)
            bitMap.clear(from.id + 23 * 2)
            bitMap.clear(from.id + 23 * 3)

            bitMap.set(to.id + 23 * amphipod.shiftTimes)
        }

        data class HashHit(val best: Long?, val costGoingIn: Long)

        // If it's stupid but it works, it's
        fun solveRec(totalCost: Long = 0, depth: Int = 0): Long? {
            val hit = hashTable[bitMap]
            if (hit != null) {
                if (totalCost >= hit.costGoingIn) {
                    return hit.best
                }
            }

            if (bitMap == SOLVED.bitMap) {
                return totalCost
            }

            var best: Long? = null

            for (source in Node.NODES) {
                if (!source.isReal) {
                    continue
                }

                val amphipod = get(source) ?: continue

                if (amphipod.homeNodes[0] == source.id && get(amphipod.homeNodes[1]) == amphipod && get(amphipod.homeNodes[2]) == amphipod && get(amphipod.homeNodes[3]) == amphipod) {
                    continue
                }

                if (amphipod.homeNodes[1] == source.id && get(amphipod.homeNodes[2]) == amphipod && get(amphipod.homeNodes[3]) == amphipod) {
                    continue
                }

                if (amphipod.homeNodes[2] == source.id && get(amphipod.homeNodes[3]) == amphipod) {
                    continue
                }

                if (amphipod.homeNodes[3] == source.id) {
                    continue
                }

                val homeSource = amphipod.homeNodes.contains(source.id)
                for (target in Node.NODES) {
                    if (homeSource && amphipod.homeNodes.contains(target.id)) {
                        continue
                    }

                    if (!target.isReal) {
                        continue
                    }

                    if (source.isOutside && target.isOutside) {
                        continue
                    }

                    if (target.isHomeRows && !amphipod.homeNodes.contains(target.id)) {
                        continue
                    }

                    if (get(target) != null) {
                        continue
                    }

                    if (source == target) {
                        continue
                    }

                    val requiredOpenTo = source.requiredOpenTo(target)

                    if (!requiredOpenTo.all(::isEmpty)) {
                        continue
                    }

                    val requiredClosed = if (target.id > 6) {
                        generateSequence(target.id) { it + 4 }.drop(1).takeWhile { it <= 22 }.map { Node.NODES[it] }.toList()
                    } else {
                        listOf()
                    }

                    if (!requiredClosed.all { get(it) == amphipod }) {
                        continue
                    }

                    val cost = (requiredOpenTo.size + 1) * amphipod.cost

                    move(source, target)

                    best = listOfNotNull(best, solveRec(totalCost + cost, depth + 1)).minOrNull()

                    move(target, source)
                }
            }

            hashTable[bitMap.clone() as BitSet] = HashHit(best, totalCost)
            return best
        }

        companion object {
            fun allAtHome(amphipods: List<Amphipod>) =
                Burrow(List(7) { null } + amphipods)

            fun encode(raw: List<Amphipod?>): BitSet {
                val result = BitSet(23 * 4)

                for ((i, amphipod) in raw.withIndex().filterNot { it.value == null }) {
                    result.set(i + 23 * amphipod!!.shiftTimes)
                }

                return result
            }

            val SOLVED =
                List(4) { listOf(AMBER, BRONZE, COPPER, DESERT) }
                    .flatten()
                    .let(::allAtHome)

            fun parse(input: List<String>) =
                input
                    .joinToString("\n")
                    .replace(Regex("[^ABCD]"), "")
                    .toCharArray()
                    .map { char -> Amphipod.values().single { it.char == char } }
                    .let(::allAtHome)
        }
    }

    data class Node(val id: Int) {
        override fun toString() = id.toString()

        private val neighbours = mutableListOf<Node>()
        private val requiredOpenTo = List<List<Node>>(27) { listOf() }.toMutableList()

        val isOutside = id <= 6
        val isHomeRows = id > 6
        val isReal = id <= 22

        fun requiredOpenTo(node: Node) = requiredOpenTo[node.id]

        companion object {
            private val EDGES = listOf(
                0 to 1,
                1 to 23,
                23 to 2,
                2 to 24,
                24 to 3,
                3 to 25,
                25 to 4,
                4 to 26,
                26 to 5,
                5 to 6,
                11 to 7,
                7 to 23,
                12 to 8,
                8 to 24,
                13 to 9,
                9 to 25,
                14 to 10,
                10 to 26,
                19 to 15,
                15 to 11,
                20 to 16,
                16 to 12,
                21 to 17,
                17 to 13,
                22 to 18,
                18 to 14,
            )

            val NODES = buildNodes();

            private fun buildNodes(): List<Node> {
                val nodes = List(27, ::Node)

                for ((from, to) in EDGES) {
                    nodes[from].neighbours.add(nodes[to]);
                    nodes[to].neighbours.add(nodes[from]);
                }


                for (source in nodes) {
                    for (target in nodes) {
                        if (source == target) {
                            continue
                        }

                        source.requiredOpenTo[target.id] = findPath(source, target).filter { it != source && it != target }
                    }
                }

                return nodes;
            }

            private fun findPath(from: Node, to: Node): List<Node> {
                return findPathRec(from, to)?.first ?: throw IllegalStateException("$from -> $to")
            }

            private fun findPathRec(current: Node, to: Node, path: MutableList<Node> = mutableListOf(current), totalCost: Int = 0): Pair<List<Node>, Int>? {
                if (current == to) {
                    return path.toList() to totalCost;
                }

                return current.neighbours.filterNot { path.contains(it) }.mapNotNull {
                    path.add(it)

                    val result = findPathRec(it, to, path, totalCost + 1)

                    path.remove(it)
                    result
                }.minByOrNull { it.second }
            }
        }
    }
}
