import java.math.BigInteger

object Day21 : Day() {
    override val part1 = object : Part<Int>(739785) {
        override fun solve(input: List<String>) =
            Game.parse(input).playWholeGame()
    }

    override val part2 = object : Part<BigInteger>(BigInteger.valueOf(444356092776315L)) {
        override fun solve(input: List<String>) =
            BitQuantumGame.parse(input).playWholeGame()
    }

    private class BitQuantumGame(p1InitialPosition: Int, p2InitialPosition: Int) {
        companion object {
            private const val P1_POS_MASK = 0b1111
            private const val P1_POS_SHIFT = 0
            private const val P2_POS_MASK = 0b11110000
            private const val P2_POS_SHIFT = 4
            private const val P1_SCORE_MASK = 0b1111100000000
            private const val P1_SCORE_SHIFT = 8
            private const val P2_SCORE_MASK = 0b111110000000000000
            private const val P2_SCORE_SHIFT = 13

            private val SCORE_TO_WIN = 21

            fun parse(input: List<String>) =
                Game
                    .parse(input)
                    .let(Game::players)
                    .let {
                        BitQuantumGame(
                            it[0].position,
                            it[1].position,
                        )
                    }
        }

        private var p1Wins = BigInteger.ZERO
        private var p2Wins = BigInteger.ZERO

        private var states: Map<Int, BigInteger> = mapOf(
            encode(
                p1InitialPosition - 1,
                p2InitialPosition - 1,
                0,
                0
            ) to BigInteger.ONE
        )

        private fun encode(
            p1Pos: Int,
            p2Pos: Int,
            p1Score: Int,
            p2Score: Int,
        ): Int {
            return p1Pos.shl(P1_POS_SHIFT).and(P1_POS_MASK) or
                    p2Pos.shl(P2_POS_SHIFT).and(P2_POS_MASK) or
                    p1Score.shl(P1_SCORE_SHIFT).and(P1_SCORE_MASK) or
                    p2Score.shl(P2_SCORE_SHIFT).and(P2_SCORE_MASK)
        }

        private val rollToCount: List<Pair<Int, BigInteger>> =
            IntRange(3, 9).zip(listOf<Long>(1, 3, 6, 7, 6, 3, 1).map(BigInteger::valueOf))

        fun step(): Boolean {
            states = buildMap {
                for ((state, currentCount) in states) {
                    val p1Score = state.and(P1_SCORE_MASK).shr(P1_SCORE_SHIFT)
                    val p2Score = state.and(P2_SCORE_MASK).shr(P2_SCORE_SHIFT)
                    if (p1Score == SCORE_TO_WIN || p2Score == SCORE_TO_WIN) {
                        throw IllegalStateException()
                    }

                    val p1Pos = state.and(P1_POS_MASK).shr(P1_POS_SHIFT)
                    val p2Pos = state.and(P2_POS_MASK).shr(P2_POS_SHIFT)

                    for ((roll, incrementCount) in rollToCount) {
                        val newPos = (p1Pos + roll) % 10
                        val newScore = p1Score + newPos + 1

                        if (newScore >= SCORE_TO_WIN) {
                            p1Wins = p1Wins.plus(currentCount * incrementCount)
                        } else {
                            val newKey = encode(
                                newPos,
                                p2Pos,
                                newScore,
                                p2Score
                            )
                            compute(newKey) { _, current ->
                                (current ?: BigInteger.ZERO).plus(currentCount * incrementCount)
                            }
                        }
                    }
                }
            }

            states = buildMap {
                for ((state, currentCount) in states) {
                    val p1Score = state.and(P1_SCORE_MASK).shr(P1_SCORE_SHIFT)
                    val p2Score = state.and(P2_SCORE_MASK).shr(P2_SCORE_SHIFT)
                    if (p1Score == SCORE_TO_WIN || p2Score == SCORE_TO_WIN) {
                        throw IllegalStateException()
                    }

                    val p1Pos = state.and(P1_POS_MASK).shr(P1_POS_SHIFT)
                    val p2Pos = state.and(P2_POS_MASK).shr(P2_POS_SHIFT)

                    for ((roll, incrementCount) in rollToCount) {
                        val newPos = (p2Pos + roll) % 10
                        val newScore = p2Score + newPos + 1

                        if (newScore >= SCORE_TO_WIN) {
                            p2Wins = p2Wins.plus(currentCount * incrementCount)
                        } else {
                            val newKey = encode(
                                p1Pos,
                                newPos,
                                p1Score,
                                newScore
                            )
                            compute(newKey) { _, current ->
                                (current ?: BigInteger.ZERO).plus(currentCount * incrementCount)
                            }
                        }
                    }
                }
            }

            return states.isEmpty()
        }

        fun playWholeGame(): BigInteger {
            return generateSequence { step() }
                .first { it }
                .let { p1Wins.max(p2Wins) }
        }
    }

    private class Game(players: List<Player>) {
        private val die = DeterministicDie()
        val players = players.toList()

        fun playWholeGame(): Int {
            while (true) {
                step()?.also {
                    return it
                }
            }
        }

        private fun step(): Int? {
            players.forEach { player ->
                player.moveForward(die() + die() + die())
                player.score += player.position

                if (player.score >= 1000) {
                    return players.single { it != player }.score * die.numberOfRolls
                }
            }

            return null
        }

        companion object {
            fun parse(input: List<String>) =
                input.map { line ->
                    Regex("""Player \d+ starting position: (?<pos>\d+)""").matchEntire(line)!!.groups.let {
                        Player(it["pos"]!!.value.toInt())
                    }
                }.let(::Game)
        }

        class Player(position: Int) {
            init {
                check((1..10).contains(position))
            }

            fun moveForward(value: Int) {
                position = ((position - 1 + value) % 10) + 1
            }

            var position: Int = position
                private set

            var score: Int = 0
        }

        private class DeterministicDie : () -> Int {
            var numberOfRolls = 0
                private set
            private var current = 1

            override fun invoke() =
                current
                    .also { current = (current % 100) + 1 }
                    .also { numberOfRolls++ }
        }
    }
}