object Day16 : Day() {
    override val part1 = object : Part<Int>(20) {
        override fun solve(input: List<String>): Int {
            return Packet.parse(input.single()).versionSum()
        }

        fun Packet.versionSum(): Int {
            return when (this) {
                is Packet.Bits -> version + packets.sumOf { it.versionSum() }
                is Packet.Packets -> version + packets.sumOf { it.versionSum() }
                is Packet.Literal -> version
            }
        }
    }

    override val part2 = object : Part<Long>(1L) {
        override fun solve(input: List<String>): Long {
            return Packet.parse(input.single()).also(::println).value
        }
    }

    private sealed interface Packet {
        val version: Int
        val typeId: Int
        val length: Int

        val operator: Operator
            get() = Operator.fromId(typeId)

        val value: Long


        data class Bits(
            override val version: Int,
            override val typeId: Int,
            val numBitsParsed: Int,
            val packets: List<Packet>,
        ) : Packet {
            override val length = 3 + 3 + 1 + 15 + packets.sumOf(Packet::length)
            override val value by lazy { packets.map(Packet::value).let { operator.evaluate(it) } }

            override fun toString(): String {
                return "BitsPackage__(version=$version, typeId=$typeId, bits=$numBitsParsed)=$value\n" +
                        packets.joinToString("\n") {
                            it.toString().prependIndent("    ")
                        }
            }
        }


        data class Packets(
            override val version: Int,
            override val typeId: Int,
            val numPacketsParsed: Int,
            val packets: List<Packet>,
        ) : Packet {
            override val length = 3 + 3 + 1 + 11 + packets.sumOf(Packet::length)
            override val value by lazy { packets.map(Packet::value).let { operator.evaluate(it) } }
            override fun toString(): String {
                return "PacketsPacket(version=$version, typeId=$typeId, packets=$numPacketsParsed)=$value\n" +
                        packets.joinToString("\n") {
                            it.toString().prependIndent("    ")
                        }
            }
        }

        data class Literal(
            override val version: Int,
            override val typeId: Int,
            val chunks: List<Chunk>,
        ) : Packet {
            override val length = 3 + 3 + chunks.sumOf(Chunk::length)
            override val value by lazy {
                val dataChunks = chunks.count { it is Chunk.Data }

                chunks.filterIsInstance<Chunk.Data>().mapIndexed { index, data ->
                    println(dataChunks)
                    println(index)

                    data.value.shl((dataChunks - 1 - index) * 4)
                }.sum()
            }

            override fun toString(): String {
                return "Literal______(version=$version, typeId=$typeId, length=$length)=$value\n" +
                        chunks.joinToString("\n") {
                            it.toString().prependIndent("    ")
                        }
            }
        }

        companion object {
            fun parse(input: String): Packet {
                return parsePacket(decodeHexadecimal(input))
            }

            private fun parsePacket(representation: CharSequence): Packet {
                val input = StringBuilder(representation)

                val version = input.removeN(3).toInt(2)
                val typeId = input.removeN(3).toInt(2)

                if (typeId == 4) {
                    val windowed = input.windowed(5, 5, true)

                    val result = mutableListOf<Chunk>()

                    for (s in windowed) {
                        if (s.length < 5) {
                            result.add(Chunk.Remainder(s.length))
                        } else {
                            result.add(Chunk.Data(s))

                            if (s.startsWith("0")) {
                                break
                            }
                        }
                    }

                    return Packet.Literal(
                        version,
                        typeId,
                        result,
                    )
                } else {
                    val lengthTypeId = input.removeN(1)
                    return when (lengthTypeId) {
                        "0" -> {
                            val totalLength = input.removeN(15).toInt(2)
                            val packets = StringBuilder(input.removeN(totalLength))

                            var parsed = 0

                            val a = mutableListOf<Packet>()

                            while (parsed < totalLength) {
                                val parsePacket = parsePacket(packets)
                                parsed += parsePacket.length
                                a += parsePacket

                                packets.removeN(parsePacket.length)
                            }


                            val l = 3 + 3 + 1 + 15 + a.sumOf(Packet::length)

                            Packet.Bits(
                                version,
                                typeId,
                                totalLength,
                                a
                            )
                        }
                        "1" -> {
                            val numberOfPackets = input.removeN(11).toInt(2)

                            val packets = StringBuilder(input)

                            val result = mutableListOf<Packet>()

                            for (i in 0 until numberOfPackets) {
                                val parsePacket = parsePacket(packets)
                                result += parsePacket

                                packets.removeN(parsePacket.length)
                            }

                            val l = 3 + 3 + 1 + 11 + result.sumOf(Packet::length)

                            Packet.Packets(
                                version,
                                typeId,
                                numberOfPackets,
                                result
                            )
                        }
                        else -> throw IllegalStateException()
                    }
                }
            }

            private fun StringBuilder.removeN(n: Int): String {
                val substring = substring(0, n)

                for (i in 0 until n) {
                    deleteCharAt(0)
                }

                return substring
            }

            private fun decodeHexadecimal(hexadecimal: String): String {
                return hexadecimal
                    .chunked(1)
                    .asSequence()
                    .map { it.toInt(16) }
                    .map { it.toString(2) }
                    .map { "%4s".format(it) }
                    .map { it.replace(" ", "0") }
                    .joinToString("")
            }
        }

    }

    private sealed interface Chunk {
        val length: Int

        data class Remainder(override val length: Int) : Chunk {
            override fun toString(): String {
                return "Rem_(${" ".repeat(length)})"
            }
        }

        data class Data(val representation: String) : Chunk {
            override val length = representation.length

            val flag = representation.substring(0, 1)
            val content = representation.substring(1)
            val value = content.toLong(2)

            override fun toString(): String {
                return "Data([$flag $content]=$value)"
            }
        }
    }

    private sealed class Operator(val evaluate: (input: List<Long>) -> Long) {

        object Sum : Operator(List<Long>::sum)
        object Product : Operator({ it.fold(1) { a, b -> a * b } })
        object Minimum : Operator({ it.minOf { it } })
        object Maximum : Operator({ it.maxOf { it } })
        object GreaterThan : Operator({ (a, b) -> if (a > b) 1 else 0 })
        object LessThan : Operator({ (a, b) -> if (a < b) 1 else 0 })
        object EqualTo : Operator({ (a, b) -> if (a == b) 1 else 0 })

        companion object {
            fun fromId(id: Int): Operator {
                return when (id) {
                    0 -> Sum
                    1 -> Product
                    2 -> Minimum
                    3 -> Maximum
                    5 -> GreaterThan
                    6 -> LessThan
                    7 -> EqualTo
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }


}