import Amphipod.AMBER
import Amphipod.BRONZE
import Amphipod.COPPER
import Amphipod.DESERT

object Day23 : Day() {
    override val part1 = object : Part<Long>(12521) {
        override fun solve(input: List<String>): Long {
            val parse = Burrow.parse(input)
//            parse.test();

            return parse.solveRec()!!
        }
    }

    override val part2 = object : Part<Long>(0) {
        override fun solve(input: List<String>): Long {
            TODO("Not yet implemented")
        }
    }
}

enum class Amphipod(val shiftTimes: Int, val char: Char, val cost: Long, val homeNodes: List<Int>) {
    AMBER(0, 'A', 1, listOf(7, 11)),
    BRONZE(1, 'B', 10, listOf(8, 12)),
    COPPER(2, 'C', 100, listOf(9, 13)),
    DESERT(3, 'D', 1000, listOf(10, 14)),
}

enum class RequiredState {
    CLOSED,
    OPEN,
    ANY,
}

class Burrow(private val raw: MutableList<Amphipod?>) {
    override fun toString() =
        """
#############
#%s%s.%s.%s.%s.%s%s#
###%s#%s#%s#%s###
  #%s#%s#%s#%s#
  #########
      """.trimIndent().format(*(0 until 15).map(::get).map { it?.char ?: '.' }.toTypedArray())

    private var bitMap = encode(raw);

    private val hashTable: MutableMap<Long, HashHit> = mutableMapOf();

    private fun get(node: Node) = get(node.id)
    private fun get(id: Int): Amphipod? {
        if (id > 14) {
            return null
        } else {
            val mask = 1L.shl(id)

            return if (bitMap.and(mask) != 0L) {
                AMBER
            } else if (bitMap.and(mask.shl(16)) != 0L) {
                BRONZE
            } else if (bitMap.and(mask.shl(16 * 2)) != 0L) {
                COPPER
            } else if (bitMap.and(mask.shl(16 * 3)) != 0L) {
                DESERT
            } else {
                null
            }
        }
    }

    private fun isEmpty(node: Node) = isEmpty(node.id)
    private fun isEmpty(id: Int) = get(id) == null

    private fun move(from: Node, to: Node) {
        val amphipod = get(from.id)
        bitMap = bitMap.and(1L.shl(from.id).inv())
        bitMap = bitMap.and(1L.shl(from.id + 16).inv())
        bitMap = bitMap.and(1L.shl(from.id + 16 * 2).inv())
        bitMap = bitMap.and(1L.shl(from.id + 16 * 3).inv())

        bitMap = bitMap.or(1L.shl(to.id + 16 * amphipod!!.shiftTimes))
    }

    data class HashHit(val best: Long?, val costGoingIn: Long)

    fun solveRec(totalCost: Long = 0, depth: Int = 0): Long? {
        val costGoingIn = totalCost

//        if (depth == 4) {
//            return null
//        }

        fun println(o: Any? = null) = kotlin.io.println(o.toString().lines().map { " ".repeat(depth * 4) + it }.joinToString("\n"))

        val hit = hashTable[bitMap]
        if (hit != null) {
//            println("HIT")
//            println(fromStore)
//            println(this)
//            println()


            if (costGoingIn >= hit.costGoingIn) {
                return hit.best
            }



        }

//        Thread.sleep(10)
//
//        println(this)
//        println()
//        println()
//        println()

        if (bitMap == SOLVED_BIT_MAP) {
            return totalCost
        }

        var best: Long? = null

//        println(bitMap.toString(2))

        for (source in Node.NODES) {

            if (!source.isReal) {
                continue
            }

            val amphipod = get(source) ?: continue

            if (amphipod.homeNodes[0] == source.id && get(amphipod.homeNodes[1]) == amphipod) {
                continue
            }

            if (amphipod.homeNodes[1] == source.id) {
                continue
            }

            for (target in Node.NODES) {
                if (!target.isReal) {
//                    println("!!!A Invalid move $amphipod from $source to $target")
                    continue
                }

                if (source.isOutside && target.isOutside) {
//                    println("!!!B Invalid move $amphipod from $source to $target")
                    continue
                }

                if (target.isHomeRows && !amphipod.homeNodes.contains(target.id)) {
//                    println("!!!C Invalid move $amphipod from $source to $target")
                    continue
                }

                if (get(target) != null) {
//                    println("!!!D Invalid move $amphipod from $source to $target")
                    continue
                }

                if (source == target) {
//                    println("!!!E Invalid move $amphipod from $source to $target")
                    continue
                }

                val requiredOpenTo = source.requiredOpenTo(target)

                if (!requiredOpenTo.all(::isEmpty)) {
//                    println("!!!F Invalid move $amphipod from $source to $target ($requiredOpenTo) (${requiredOpenTo.map { isEmpty(it) }}) (${get(3)})")
//                    println(this)
                    continue
                }

                val requiredClosed = source.requiredClosedTo(target)

                if (requiredClosed != null) {
                    if (amphipod != get(requiredClosed)) {
//                        println("!!!G Invalid move $amphipod from $source to $target")

                        continue
                    }
                }

                val cost = (requiredOpenTo.size + 1) * amphipod.cost

                val bitMapBefore = bitMap

                move(source, target)

                val current = solveRec(totalCost + cost, depth + 1)

//                if (
//                    depth == 0 && bitMap==1441188164650944512 || depth == 1 && bitMap == 1441189264162572288 || depth == 2 && bitMap == 290519559369410560 || depth == 3 && bitMap == 290519559637583872
//                ) {
//                    println("$depth Valid move $amphipod from $source to $target for $cost (scored $current) ($bitMap)")
//                    println(this)
//                    kotlin.io.println()
//                }

                best = listOfNotNull(best, current).minOrNull()
                if (depth <= 1) {
//                    println("Valid move $amphipod from $source to $target for $cost")
//                    println(current)
//                    println(best)
//                    println(this)
                }

                move(target, source)
            }
        }

        hashTable[bitMap] = HashHit(best, costGoingIn)
        return best
    }

    fun test() {
        println(this)
        move(Node.NODES[9], Node.NODES[2])
        println(this)

        TODO()
    }

    companion object {
        fun encode(raw: List<Amphipod?>): Long {
            var result = 0L

            for ((i, amphipod) in raw.withIndex().filterNot { it.value == null }) {
                result = result.or(1L.shl(i).shl(16 * amphipod!!.shiftTimes))
            }

            return result
        }

        val SOLVED =
            Burrow((List(7) { null } + listOf(AMBER, BRONZE, COPPER, DESERT, AMBER, BRONZE, COPPER, DESERT)).toMutableList())

        val SOLVED_BIT_MAP = encode(SOLVED.raw)

        fun parse(input: List<String>) =
            Burrow((List(7) { null } + input.joinToString("\n").replace(Regex("[^ABCD]"), "").toCharArray().map { char -> Amphipod.values().single { it.char == char } }).toMutableList())
    }
}

data class Node(val id: Int) {
    override fun toString() = id.toString()

    private val neighbours = mutableListOf<Node>()
    private val requiredOpenTo = List<List<Node>>(19) { listOf() }.toMutableList()
    private val requiredClosedTo = List<Node?>(19) { null }.toMutableList()

    val isOutside = id <= 6
    val isHomeRows = id > 6
    val isReal = id <= 14

    fun requiredOpenTo(node: Node) = requiredOpenTo[node.id]
    fun requiredClosedTo(node: Node) = requiredClosedTo[node.id]

    companion object {
        val NODES = buildNodes();

        private fun buildNodes(): List<Node> {
            val nodes = List(19, ::Node)

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
                    if (target.id == 7) {
                        source.requiredClosedTo[target.id] = nodes[11]
                    } else if (target.id == 8) {
                        source.requiredClosedTo[target.id] = nodes[12]
                    } else if (target.id == 9) {
                        source.requiredClosedTo[target.id] = nodes[13]
                    } else if (target.id == 10) {
                        source.requiredClosedTo[target.id] = nodes[14]
                    }
                }
            }

//            for (node in nodes) {
//                println(node.id)
//                println(node.neighbours)
//                println(node.requiredOpenTo)
//                println(node.requiredClosedTo)
//            }


            return nodes;
        }

        private fun findPath(from: Node, to: Node): List<Node> {
            return findPathRec(from, to)!!.first
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

private val EDGES = listOf(
    0 to 1,
    1 to 15,
    15 to 2,
    2 to 16,
    16 to 3,
    3 to 17,
    17 to 4,
    4 to 18,
    18 to 5,
    5 to 6,
    11 to 7,
    7 to 15,
    12 to 8,
    8 to 16,
    13 to 9,
    9 to 17,
    14 to 10,
    10 to 18,
)
