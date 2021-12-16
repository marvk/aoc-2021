object Day16 : Day() {
    override val part1 = object : Part<Int>(20) {
        override fun solve(input: List<String>) =
            Packet.parse(input.single()).versionSum()

        private fun Packet.versionSum(): Int =
            when (this) {
                is Packet.Composite -> version + packets.sumOf { it.versionSum() }
                is Packet.Literal -> version
            }
    }

    override val part2 = object : Part<Long>(1L) {
        override fun solve(input: List<String>) =
            Packet.parse(input.single()).value
    }

    sealed interface Packet {
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
            override val length = META_LENGTH + 16 + packets.sumOf(Packet::length)

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
            override val length = META_LENGTH + 12 + packets.sumOf(Packet::length)

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
            override val length = META_LENGTH + chunks.sumOf(Chunk::length)

            override val value by lazy {
                chunks
                    .mapIndexed { index, data ->
                        data.value.shl((chunks.size - 1 - index) * 4)
                    }
                    .sum()
            }

            override fun toString(): String {
                return "Literal______(version=$version, typeId=$typeId, length=$length)=$value\n" +
                        chunks.joinToString("\n") {
                            it.toString().prependIndent("    ")
                        }
            }
        }

        companion object {
            private const val META_LENGTH = 3 + 3
            private const val BIT_NUMBER_BITS = 15
            private const val PACKET_NUMBER_BITS = 11

            fun parse(input: String) =
                parsePacket(decodeHexToBin(input))

            private fun parsePacket(representation: CharSequence): Packet {
                val input = StringBuilder(representation)
                val version = input.removeN(3).toInt(2)

                return when (val typeId = input.removeN(3).toInt(2)) {
                    4 -> Literal(version, typeId, parseLiteralPacket(input))
                    else -> when (input.removeN(1)) {
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

            private fun parseLiteralPacket(input: CharSequence): List<Chunk> =
                buildList {
                    for (s in input.chunked(5)) {
                        add(Chunk(s))

                        if (s.startsWith("0")) {
                            break
                        }
                    }
                }

            private fun parsePacketsPacket(input: CharSequence): Pair<Int, List<Packet>> {
                val numberOfPackets = input.substring(0, PACKET_NUMBER_BITS).toInt(2)
                val packetsInput = StringBuilder(input.substring(PACKET_NUMBER_BITS))

                return (0 until numberOfPackets)
                    .map {
                        parsePacket(packetsInput).also { packetsInput.removeN(it.length) }
                    }.let {
                        numberOfPackets to it
                    }
            }

            private fun parseBitPacket(input: CharSequence): Pair<Int, List<Packet>> {
                val expectedBitLength = input.substring(0, BIT_NUMBER_BITS).toInt(2)
                val packetsInput = StringBuilder(input.substring(BIT_NUMBER_BITS, BIT_NUMBER_BITS + expectedBitLength))

                return buildList {
                    while (sumOf(Packet::length) < expectedBitLength) {
                        parsePacket(packetsInput)
                            .also { add(it) }
                            .also { packetsInput.removeN(it.length) }
                    }
                }.let {
                    Pair(expectedBitLength, it)
                }
            }

            private fun StringBuilder.removeN(n: Int) =
                substring(0, n).also {
                    (0 until n).forEach { _ -> deleteCharAt(0) }
                }

            private fun decodeHexToBin(hexadecimal: String) =
                hexadecimal
                    .chunked(1)
                    .asSequence()
                    .map { it.toInt(16) }
                    .map { it.toString(2) }
                    .map { "%4s".format(it) }
                    .map { it.replace(" ", "0") }
                    .joinToString("")
        }

    }

    data class Chunk(val representation: String) {
        val length = representation.length

        val flag = representation.substring(0, 1)
        val content = representation.substring(1)
        val value = content.toLong(2)

        override fun toString(): String {
            return "Data(flag=$flag content=$content)=$value"
        }
    }

    sealed class Operator(private val string: String, val evaluate: (input: List<Long>) -> Long) {

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