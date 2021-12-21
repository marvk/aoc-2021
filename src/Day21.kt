object Day21 : Day() {
    override val part1 = object : Part<Int>(739785) {
        override fun solve(input: List<String>) =
            Game.parse(input).playWholeGame()
    }


    override val part2 = object : Part<Long>(444356092776315L) {
        override fun solve(input: List<String>) =
            QuantumGame.parse(input).playWholeGame()
    }

    private class QuantumGame(gameState: GameState) {
        private var states = mapOf<GameState, Long>(gameState to 1)

        fun playWholeGame() =
            generateSequence(::step)
                .first { it }
                .let { states }
                .entries
                .partition { it.key.player1.score >= SCORE_TO_WIN }
                .toList()
                .map { it.sumOf(Map.Entry<GameState, Long>::value) }
                .maxOf { it }

        private fun step(): Boolean {
            play(GameState::player1) { state, newPosition ->
                state.copy(player1 = state.player1.copy(position = newPosition))
            }
            score(GameState::player1) { state, newScore, won ->
                state.copy(player1 = state.player1.copy(score = newScore), won = won)
            }
            play(GameState::player2) { state, newPosition ->
                state.copy(player2 = state.player2.copy(position = newPosition))
            }
            score(GameState::player2) { state, newScore, won ->
                state.copy(player2 = state.player2.copy(score = newScore), won = won)
            }

            return states.keys.all(GameState::won)
        }

        private fun score(
            playerState: (GameState) -> PlayerState,
            copyState: (GameState, newScore: Int, won: Boolean) -> GameState,
        ) {
            buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        incrementBy(state, count)
                    } else {
                        val newScore = playerState(state).score + playerState(state).position
                        val won = newScore >= SCORE_TO_WIN
                        incrementBy(copyState(state, newScore, won), count)
                    }
                }
            }.also { states = it }
        }

        private fun play(
            playerState: (GameState) -> PlayerState,
            copyState: (GameState, newPosition: Int) -> GameState,
        ) {
            buildMap {
                states.forEach { (state, count) ->
                    if (state.won) {
                        incrementBy(state, count)
                    } else {
                        rollToCount.forEach { (roll, incrementBy) ->
                            newPosition(playerState(state).position, roll)
                                .let { copyState(state, it) }
                                .also { incrementBy(it, incrementBy * count) }
                        }
                    }
                }
            }.also { states = it }
        }

        private fun MutableMap<GameState, Long>.incrementBy(gameState: GameState, count: Long) =
            compute(gameState) { _, previousCount ->
                (previousCount ?: 0) + count
            }

        private val rollToCount = IntRange(3, 9).zip(listOf(1, 3, 6, 7, 6, 3, 1))

        private fun newPosition(previousPosition: Int, increment: Int) = ((previousPosition - 1 + increment) % 10) + 1

        companion object {
            private val SCORE_TO_WIN = 21

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