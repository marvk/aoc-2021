object Day16 : Day() {
    override val part1 = object : Part<Int>(20) {
        override fun solve(input: List<String>) =
            Packet.parse(input.single()).versionSum()

        fun Packet.versionSum(): Int =
            when (this) {
                is Packet.Composite -> version + packets.sumOf { it.versionSum() }
                is Packet.Literal -> version
            }
    }

    override val part2 = object : Part<Long>(1L) {
        override fun solve(input: List<String>) =
            Packet.parse(input.single()).also(::println).value
    }

    private sealed interface Packet {
        val version: Int
        val typeId: Int
        val length: Int
        val value: Long

        sealed class Composite : Packet {
            abstract val packets: List<Packet>
            override val value by lazy { packets.map(Packet::value).let { operator.evaluate(it) } }
            val operator: Operator
                get() = Operator.fromId(typeId)
        }

        data class Bits(
            override val version: Int,
            override val typeId: Int,
            val numBitsParsed: Int,
            override val packets: List<Packet>,
        ) : Composite() {
            override val length = metaLength + 16 + packets.sumOf(Packet::length)

            override fun toString(): String {
                return "BitsPackage__(version=$version, typeId=$typeId, bits=$numBitsParsed, operator=$operator)=$value\n" +
                        packets.joinToString("\n") {
                            it.toString().prependIndent("    ")
                        }
            }
        }

        data class Packets(
            override val version: Int,
            override val typeId: Int,
            val numPacketsParsed: Int,
            override val packets: List<Packet>,
        ) : Composite() {
            override val length = metaLength + 12 + packets.sumOf(Packet::length)

            override fun toString(): String {
                return "PacketsPacket(version=$version, typeId=$typeId, packets=$numPacketsParsed, operator=$operator)=$value\n" +
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
            private val numDataChunks = chunks.count { it is Chunk.Data }

            override val length = metaLength + chunks.sumOf(Chunk::length)

            override val value by lazy {
                chunks
                    .filterIsInstance<Chunk.Data>()
                    .mapIndexed { index, data ->
                        data.value.shl((numDataChunks - 1 - index) * 4)
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
            private const val metaLength = 3 + 3
            private const val bitLengthBits = 15
            private const val packetLengthBits = 11

            fun parse(input: String): Packet {
                return parsePacket(decodeHexadecimal(input))
            }

            private fun parsePacket(representation: CharSequence): Packet {
                val input = StringBuilder(representation)

                val version = input.removeN(3).toInt(2)
                val typeId = input.removeN(3).toInt(2)

                if (typeId == 4) {
                    return Literal(
                        version,
                        typeId,
                        literalChunks(input),
                    )
                } else {
                    return when (input.removeN(1)) {
                        "0" -> parseBitPacket(input).let { (expectedNumberOfBits, packets) ->
                            Bits(version, typeId, expectedNumberOfBits, packets)
                        }
                        "1" -> parsePacketsPacket(input).let { (expectedNumberOfPackets, packets) ->
                            Packets(version, typeId, expectedNumberOfPackets, packets)
                        }
                        else -> throw IllegalStateException()
                    }
                }
            }

            private fun parsePacketsPacket(input: CharSequence): Pair<Int, MutableList<Packet>> {
                val numberOfPackets = input.substring(0, packetLengthBits).toInt(2)
                val packetsInput = StringBuilder(input.substring(packetLengthBits))

                return mutableListOf<Packet>().apply {
                    for (i in 0 until numberOfPackets) {
                        val parsePacket = parsePacket(packetsInput)
                        add(parsePacket)

                        packetsInput.removeN(parsePacket.length)
                    }
                }.let {
                    Pair(numberOfPackets, it)
                }
            }

            private fun parseBitPacket(input: CharSequence): Pair<Int, MutableList<Packet>> {
                val expectedBitLength = input.substring(0, bitLengthBits).toInt(2)
                val packetsInput = StringBuilder(input.substring(bitLengthBits, bitLengthBits + expectedBitLength))

                return mutableListOf<Packet>().apply {
                    while (sumOf(Packet::length) < expectedBitLength) {
                        parsePacket(packetsInput)
                            .also { add(it) }
                            .also { packetsInput.removeN(it.length) }
                    }
                }.let {
                    Pair(expectedBitLength, it)
                }
            }

            private fun literalChunks(input: StringBuilder): List<Chunk> =
                mutableListOf<Chunk>().apply {
                    for (s in input.windowed(5, 5, true)) {
                        if (s.length < 5) {
                            add(Chunk.Remainder(s.length))
                        } else {
                            add(Chunk.Data(s))

                            if (s.startsWith("0")) {
                                break
                            }
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

    private sealed class Operator(private val string: String, val evaluate: (input: List<Long>) -> Long) {

        object Sum : Operator("plus", List<Long>::sum)
        object Product : Operator("times", { it.fold(1) { a, b -> a * b } })
        object Minimum : Operator("min", { it.minOf { it } })
        object Maximum : Operator("max", { it.maxOf { it } })
        object GreaterThan : Operator("gt", { (a, b) -> if (a > b) 1 else 0 })
        object LessThan : Operator("lt", { (a, b) -> if (a < b) 1 else 0 })
        object EqualTo : Operator("eq", { (a, b) -> if (a == b) 1 else 0 })

        override fun toString(): String {
            return string
        }

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