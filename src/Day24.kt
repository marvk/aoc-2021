import kotlin.random.Random

object Day24 : Day() {
    override val part1 = object : Part<Long>(0, skipTest = true) {
        override fun solve(input: List<String>): Long {
            val parse = Alu.parse(input)
            val alus = parse.splitIntoSingleInputAlus()

            solve(alus)

            TODO()

//            return alus.joinToString("") { alu ->
//                (0L..9L)
//                    .map(::listOf)
//                    .findLast {
//                        val evaluate = alu.evaluate(it)
//                        println(evaluate)
//                        evaluate[resultRegister] == 0L
//                    }
//                    .toString()
//            }.toLong()
        }
    }

    private val zReg = Alu.Register.Named("z")

    fun solve(alus: List<Alu>) {
        val backtrack = backtrack(alus, listOf(), 0L)

        println(backtrack)
    }

    private fun backtrack(remainingAlus: List<Alu>, result: List<Int>, expectedResult: Long): List<Int>? {
        println("result = ${result}")
        println("expectedResult = ${expectedResult}")

        val depth = result.size

        println("depth = ${depth}")
        println()
        println()

        if (remainingAlus.isEmpty()) {
            return result
        }

        val alu = remainingAlus.last()
        val nextRemainingAlus = remainingAlus.dropLast(1)

        (0L..100L).forEach { z ->
            (1L..9L).reversed().forEach { input ->
                val zResult = alu.evaluate(listOf(input), mapOf(zReg to z))[zReg]

                if (zResult == expectedResult) {
                    val backtrack = backtrack(nextRemainingAlus, result + input.toInt(), z)

                    if (backtrack != null) {
                        return backtrack
                    }
                }
            }
        }

        return null
    }

    class Alu(private val instructions: List<Instruction>) {
        override fun toString() =
            instructions.joinToString("\n", "Alu[\n", "\n]")

        fun evaluate(input: List<Long>, initialState: Map<Register.Named, Long> = mapOf()) =
            AluCalculation.evaluate(input, instructions, initialState)

        fun splitIntoSingleInputAlus(): List<Alu> {
            var current = mutableListOf<Instruction>(instructions.first())
            val result = mutableListOf<Alu>()

            for (instruction in instructions.drop(1)) {
                if (instruction is Instruction.Inp) {
                    result += Alu(current)
                    current = mutableListOf(instruction)
                } else {
                    current += instruction
                }
            }
            result += Alu(current)

            return result.toList()
        }

        private class AluCalculation private constructor(input: List<Long>, initialState: Map<Register.Named, Long>) {
            val input = input.toMutableList()

            val registers: MutableMap<Register.Named, Long> = initialState.toMutableMap()

            fun Register.value(): Long {
                return when (this) {
                    is Register.Named -> registers[this] ?: 0
                    is Register.Number -> this.value
                }
            }


            fun readInput() =
                input.first().also { input.removeFirst() }

            companion object {
                fun evaluate(
                    input: List<Long>,
                    instruction: List<Instruction>,
                    initialState: Map<Register.Named, Long> = mapOf(),
                ) =
                    AluCalculation(input, initialState)
                        .apply {
                            instruction.forEach {
                                it.run(this)
                            }
                        }
                        .registers
                        .toMap()
            }
        }

        sealed class Instruction(val run: AluCalculation.() -> Unit) {
            data class Inp(val to: Register.Named) : Instruction({
                registers[to] = readInput()
            })

            data class Add(val augend: Register.Named, val addend: Register) : Instruction({
                eval(augend, addend, Long::plus)
            })

            data class Mul(val multiplier: Register.Named, val multiplicand: Register) : Instruction({
                eval(multiplier, multiplicand, Long::times)
            })

            data class Div(val dividend: Register.Named, val divisor: Register) : Instruction({
                eval(dividend, divisor, Long::div)
            })

            data class Mod(val dividend: Register.Named, val divisor: Register) : Instruction({
                eval(dividend, divisor, Long::mod)
            })

            data class Eql(val left: Register.Named, val right: Register) : Instruction({
                eval(left, right) { r1, r2 ->
                    if (r1 == r2) 1 else 0
                }
            })

            companion object {
                private fun AluCalculation.eval(
                    register1: Register.Named,
                    register2: Register,
                    operation: (Long, Long) -> Long,
                ) {
                    registers[register1] = operation(register1.value(), register2.value())
                }

                fun parse(input: String) =
                    input.split(" ").let {
                        val op = it[0]
                        val r1 by lazy { Register.parseNamed(it[1]) }
                        val r2 by lazy { Register.parse(it[2]) }

                        when (op) {
                            "inp" -> Inp(r1)
                            "add" -> Add(r1, r2)
                            "mul" -> Mul(r1, r2)
                            "div" -> Div(r1, r2)
                            "mod" -> Mod(r1, r2)
                            "eql" -> Eql(r1, r2)
                            else -> throw IllegalArgumentException()
                        }
                    }
            }
        }

        sealed interface Register {
            data class Number(val value: Long) : Register
            data class Named(val name: String) : Register

            companion object {
                fun parse(input: String) =
                    when (val value = input.toLongOrNull()) {
                        null -> Named(input)
                        else -> Number(value)
                    }

                fun parseNamed(input: String) =
                    when (val result = parse((input))) {
                        is Named -> result
                        else -> throw IllegalArgumentException()
                    }
            }
        }

        companion object {
            fun parse(input: List<String>) =
                input.map(Instruction::parse).let(::Alu)
        }
    }

    override val part2: Part<*>
        get() = TODO("Not yet implemented")
}

@Suppress("KotlinConstantConditions")
fun main() {
//
//    val input_ = """
//inp w
//mul x 0
//add x z
//mod x 26
//div z 1
//add x 10
//eql x w
//eql x 0
//mul y 0
//add y 25
//mul y x
//add y 1
//mul z y
//mul y 0
//add y w
//add y 12
//mul y x
//add z y
//    """.trimIndent().lines()
//
//    val alu = Day24.Alu.parse(input_)
//
//    (1L..9L).map(::listOf).map {
//        alu.evaluate(it)
//    }.forEach(::println)
//
//    fun readInput() = 1
//
//
//    println("#".repeat(100))


    (-1000L..1000L).forEach { z ->
        (1..9).forEach { input ->
            if ((15..23).contains(test(z.toInt(), input)[3])) {
                println("$z    $input")
            }

        }
    }

    repeat(1000) {
        val states = generateSequence { Random.nextInt(-100, 100) }.take(4).toList()
        val input = Random.nextInt(9) + 1
//        val base = base(states, input)
        val test = test(states[3], input)
//        println(base)
//        println(test)
//        check(base == test)

        val previousZ = states[3]
        if (test(previousZ, input)[3] == 24) {
            println("$previousZ    $input")
        }

    }

//
//    var w = 0
//    var x = 0
//    var y = 0
//    var z = 0
//    fun p() {
//        println("{Named(name=w)=${w}, Named(name=x)=${x}, Named(name=z)=${z}, Named(name=y)=${y}}")
//    }
//    w = readInput()
//    x *= 0
//    x += z
//    x %= 26
//    z /= 1
//    x += 10
//    x = if (x == w) 1 else 0
//    x = if (x == 0) 1 else 0
//    y *= 0
//    y += 25
//    y *= x
//    y += 1
//    z *= y
//    y *= 0
//    y += w
//    y += 12
//    y *= x
//    z += y


}

fun test(previousZ: Int, input: Int): List<Int> {
    val v1 = 26
    val v2 = -4
    val v3 = 4

//    val v1 = 26
//    val v2 = -14
//    val v3 = 13


    val x = if (input != (previousZ % 26) + v2) 1 else 0
    val z = ((previousZ / v1) * ((25 * x) + 1)) + (input + v3) * x

    return listOf(0, x, 0, z)
}

fun base(states: List<Int>, input: Int): List<Int> {
    var w = states[0]
    var x = states[1]
    var y = states[2]
    var z = states[3]
    w = input
    x *= 0
    x += z
    x %= 26
    z /= 1
    x += 10
    x = if (x == w) 1 else 0
    x = if (x == 0) 1 else 0
    y *= 0
    y += 25
    y *= x
    y += 1
    z *= y
    y *= 0
    y += w
    y += 12
    y *= x
    z += y
    return listOf(0, x, 0, z)
}