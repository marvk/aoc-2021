import kotlin.math.max

object Day21 : Day() {
    override val part1 = object : Part<Int>(739785) {
        override fun solve(input: List<String>) =
            Game.parse(input).playWholeGame()
    }


    override val part2 = object : Part<Long>(444356092776315L) {
        override fun solve(input: List<String>) =
            QuantumGame.parse(input).playWholeGame()
    }

    class QuantumGame(val gameState: GameState) {
        private var states = mapOf<GameState, Long>(gameState to 1)

        private fun step(): Boolean {
            playPlayer1()
            playPlayer1()
            playPlayer1()
            scorePlayer1()

            playPlayer2()
            playPlayer2()
            playPlayer2()
            scorePlayer2()

            return states.keys.all { it.won }
        }

        private fun scorePlayer1() {
            states = buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        compute(state) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    } else {
                        val newScore = state.player1.score + state.player1.position
                        val won = newScore >= 21
                        compute(state.copy(player1 = state.player1.copy(score = newScore),
                            won = won)) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    }
                }
            }
        }

        private fun scorePlayer2() {
            states = buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        compute(state) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    } else {
                        val newScore = state.player2.score + state.player2.position
                        val won = newScore >= 21
                        compute(state.copy(player2 = state.player2.copy(score = newScore),
                            won = won)) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    }
                }
            }
        }

        private fun playPlayer1() {
            states = buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        compute(state) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    } else {
                        newPositions(state.player1.position)
                            .map { state.copy(player1 = state.player1.copy(position = it)) }
                            .forEach {
                                compute(it) { _, previousCount ->
                                    (previousCount ?: 0) + count
                                }
                            }
                    }
                }
            }
        }

        private fun playPlayer2() {
            states = buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        compute(state) { _, previousCount ->
                            (previousCount ?: 0) + count
                        }
                    } else {
                        newPositions(state.player2.position)
                            .map { state.copy(player2 = state.player2.copy(position = it)) }
                            .forEach {
                                compute(it) { _, previousCount ->
                                    (previousCount ?: 0) + count
                                }
                            }
                    }
                }
            }
        }

        private fun newPositions(previousPosition: Int) = listOf(
            ((previousPosition - 1 + 1) % 10) + 1,
            ((previousPosition - 1 + 2) % 10) + 1,
            ((previousPosition - 1 + 3) % 10) + 1
        )

        fun playWholeGame(): Long {
            while (true) {
                if (step()) {
                    val (p1WinEntries, p2WinEntries) = states.entries.partition { it.key.player1.score >= 21 }

                    val p1Wins = p1WinEntries.map(Map.Entry<GameState, Long>::value).sum()
                    val p2Wins = p2WinEntries.map(Map.Entry<GameState, Long>::value).sum()

                    return max(p1Wins, p2Wins)
                }
            }
        }

        companion object {
            fun parse(input: List<String>) =
                Game
                    .parse(input)
                    .let(Game::players)
                    .let {
                        GameState(
                            it[0].position.let(::PlayerState),
                            it[1].position.let(::PlayerState),
                        )
                    }
                    .let(::QuantumGame)
        }

        data class GameState(val player1: PlayerState, val player2: PlayerState, val won: Boolean = false)
        data class PlayerState(val position: Int, val score: Int = 0)
    }

    class Game(players: List<Player>, val die: Die) {
        val players = players.toList()

        fun step(): Int? {
            players.forEach { player ->
                player.moveForward(die() + die() + die())
                player.score += player.position

                if (player.score >= 1000) {
                    return players.single { it != player }.score * die.numberOfRolls
                }
            }

            return null
        }

        fun playWholeGame(): Int {
            while (true) {
                step()?.also {
                    return it
                }
            }
        }

        companion object {
            fun parse(input: List<String>) =
                input.map { line ->
                    Regex("""Player (?<pid>\d+) starting position: (?<pos>\d+)""").matchEntire(line)!!.groups.let {
                        Player(
                            it["pid"]!!.value.toInt(),
                            it["pos"]!!.value.toInt(),
                        )
                    }
                }.let { Game(it, Die.Deterministic()) }
        }

        class Player(
            val id: Int,
            position: Int,
        ) {
            init {
                check((1..10).contains(position))
            }

            fun moveForward(value: Int) {
                position = ((position - 1 + value) % 10) + 1
            }

            var position: Int = position
                private set

            var score: Int = 0

            override fun toString(): String {
                return "Player[id=$id, position=$position, score=$score]"
            }
        }

        class DeterministicDie : () -> Int {
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