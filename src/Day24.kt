// All this commented out code is here because I used to arrive at the solution ¯\_(ツ)_/¯

// Oh, also did I mention I hate everything about this mess?

object Day24 : Day() {
    sealed interface StackOp {
        data class Push(val v: Long) : StackOp
        data class Pop(val v: Long) : StackOp
    }

    data class Step(val i1: Int, val i2: Int, val difference: Int)

    override val part1 = object : Part<Long>(0, skipTest = true) {
        override fun solve(input: List<String>): Long {
            val modifiers = parseModifiers(input)
            val steps = extracted(modifiers)

            val result = List(14) { 0 }.toMutableList()

            for ((i1, i2, difference) in steps) {
                if (difference <= 0) {
                    result[i1] = 9
                    result[i2] = 9 + difference
                } else {
                    result[i1] = 9 - difference
                    result[i2] = 9
                }
            }

            return result.joinToString("").toLong()

//            for ((i, modifier) in modifiers.withIndex()) {
//                for (z in (0L..26)) {
//                    for (i in (1L..9)) {
//                        iterate2(i, Registers(z), modifier)
//                    }
//                }
//            }


//            val r = Random(0)
//
//            repeat(10000) {
//                val i = r.nextLong(1, 10)
//                val registers = Registers(r.nextLong(0, 100000000))
//
//                val m = modifiers[r.nextInt(modifiers.size)]
//
//                val x = iterate(i, registers, m)
//                val y = iterate2(i, registers, m)
//
//                check(x == y) {
//                    listOf("Unequal outputs: ", i, registers, m, "", x, y).joinToString("\n")
//                }
//            }
//
//            println("Ok")

//            modifiers.forEach(::println)
//
//
//            println(searchRec(0, 7, Registers(0), modifiers, mutableListOf()))
        }
    }

    override val part2 = object : Part<Long>(0, skipTest = true) {
        override fun solve(input: List<String>): Long {
            val modifiers = parseModifiers(input)
            val steps = extracted(modifiers)

            val result = List(14) { 0 }.toMutableList()

            for ((i1, i2, difference) in steps) {
                if (difference <= 0) {
                    result[i1] = 1 - difference
                    result[i2] = 1
                } else {
                    result[i1] = 1
                    result[i2] = 1 + difference
                }
            }

            return result.joinToString("").toLong()
        }
    }

    private fun extracted(modifiers: List<Modifiers>): List<Step> {
        val map: List<StackOp> = modifiers.map {
            if (it.sub > 0) {
                StackOp.Push(it.add)
            } else {
                StackOp.Pop(it.sub)
            }
        }

        val stack = ArrayDeque<Pair<Int, Long>>()
        val result = mutableListOf<Step>()
        for ((i, stackOp) in map.withIndex()) {
            when (stackOp) {
                is StackOp.Push -> stack.add(i to stackOp.v)
                is StackOp.Pop -> {
                    val push = stack.removeLast()
                    val pop = i to stackOp.v

                    result.add(Step(push.first, pop.first, (pop.second + push.second).toInt()))
                }
            }
        }
        return result
    }

    private fun parseModifiers(input: List<String>): List<Modifiers> {
        val chunked = input.chunked(18)
        val different = mutableSetOf<Int>()

        for (c1 in chunked) {
            for (c2 in chunked) {
                for ((i, pair) in c1.zip(c2).withIndex()) {
                    if (pair.first != pair.second) {
                        different += i
                    }
                }
            }
        }

        return chunked.map { alu -> alu.filterIndexed { index, _ -> different.contains(index) }.map { it.split(" ").last().toLong(10) }.let { Modifiers(it[0], it[1], it[2]) } }
    }

    data class Registers(val z: Long)
    data class Modifiers(val unused: Long, val sub: Long, val add: Long)

    fun iterate2(input: Long, r: Registers, m: Modifiers): Registers {
        check(input in 1..9)

        val check = m.sub
        val offset = m.add

        var z = r.z

        val cond = (z % 26) + check != input
        if (check <= 0) {
            z /= 26
        }
        if (cond) {
            z *= 26
            z += input + offset
        }

        return Registers(z)
    }


    fun iterate(i: Long, r: Registers, m: Modifiers): Registers {
        check(i in 1..9)

        val a = m.unused
        val b = m.sub
        val c = m.add

        var w = 0L
        var x = 0L
        var y = 0L
        var z = r.z

        w = i
        x = x * 0
        x = x + z
        x = x % 26
        z = z / a
        x = x + b
        x = if (x == w) 1 else 0
        x = if (x == 0L) 1 else 0
        y = y * 0
        y = y + 25
        y = y * x
        y = y + 1
        z = z * y
        y = y * 0
        y = y + w
        y = y + c
        y = y * x
        z = z + y

        return Registers(z)
    }
}

/*

But in reality I handsolved it...

       I   A (DIV)   B (CHECK)   C (OFFSET)

       0         1          10           12     push i[ 0] + 12   ────┐
       1         1          12            7     push i[ 1] +  7   ───┐│
       2         1          10            8     push i[ 2] +  8   ──┐││
       3         1          12            8     push i[ 3] +  8   ─┐│││
       4         1          11           15     push i[ 4] + 15   ┐││││
       5        26         -16           12   pop - 16 == i[ 5]   ┘││││
       6         1          10            8     push i[ 6] +  8   ┐││││
       7        26         -11           13   pop - 11 == i[ 7]   ┘││││
       8        26         -13            3   pop - 13 == i[ 8]   ─┘│││
       9         1          13           13     push i[ 9] + 13   ┐ │││
      10        26          -8            3   pop -  8 == i[10]   ┘ │││
      11        26          -1            9   pop -  1 == i[11]   ──┘││
      12        26          -4            4   pop -  4 == i[12]   ───┘│
      13        26         -14           13   pop - 14 == i[13]   ────┘


    i[ 0] + 12 - 14 == i[13]
    i[ 1] +  7 -  4 == i[12]
    i[ 2] +  8 -  1 == i[11]
    i[ 3] +  8 - 13 == i[ 8]
    i[ 4] + 15 - 16 == i[ 5]
    i[ 6] +  8 - 11 == i[ 7]
    i[ 9] + 13 -  8 == i[10]

    i[ 0] -  2 == i[13] -> (9, 7)
    i[ 1] +  3 == i[12] -> (6, 9)
    i[ 2] +  7 == i[11] -> (2, 9)
    i[ 3] -  5 == i[ 8] -> (9, 4)
    i[ 4] -  1 == i[ 5] -> (9, 8)
    i[ 6] -  3 == i[ 7] -> (9, 6)
    i[ 9] +  5 == i[10] -> (4, 9)


    i[ 0] -  2 == i[13] -> (3, 1)
    i[ 1] +  3 == i[12] -> (1, 4)
    i[ 2] +  7 == i[11] -> (1, 8)
    i[ 3] -  5 == i[ 8] -> (6, 1)
    i[ 4] -  1 == i[ 5] -> (2, 1)
    i[ 6] -  3 == i[ 7] -> (4, 1)
    i[ 9] +  5 == i[10] -> (1, 6)

 */
