package net.badgersmc.votes.application

import net.badgersmc.votes.domain.VotePartyState
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class EVAdminCommand(
    private val votePartyService: VotePartyService,
    private val voteRepository: VoteRepository,
) {
    private val mm = MiniMessage.miniMessage()

    private val header = mm.deserialize(
        "<shadow:#000000:1><gold>=== EnthusiaVotes Admin ===</gold></shadow>"
    )
    private val unknown = mm.deserialize(
        "<red>Unknown subcommand. Use forceparty, stats, or reload.</red>"
    )

    fun execute(playerName: String, playerUuid: UUID, args: List<String>): Component {
        if (args.isEmpty()) return unknown
        return when (args[0].lowercase()) {
            "forceparty" -> forceParty()
            "stats" -> serverStats()
            "party" -> partyStatus()
            else -> unknown
        }
    }

    private fun forceParty(): Component {
        votePartyService.activate()
        return mm.deserialize(
            "<shadow:#000000:1><green>Vote Party manually activated!</green></shadow>"
        )
    }

    private fun serverStats(): Component {
        val totalVotes = voteRepository.getTotalServerVotes()
        val topVoters = voteRepository.getTopVoters(5)

        val lines = mutableListOf(header)
        lines.add(
            mm.deserialize(
                "<shadow:#000000:1> <gray>Total Server Votes:</gray> <white>$totalVotes</white></shadow>"
            )
        )

        if (topVoters.isEmpty()) {
            lines.add(mm.deserialize("<shadow:#000000:1> <gray>No votes yet.</gray></shadow>"))
        } else {
            lines.add(mm.deserialize("<shadow:#000000:1><gold>Top Voters:</gold></shadow>"))
            for ((i, stats) in topVoters.withIndex()) {
                val safe = mm.escapeTags(stats.playerUuid.toString().take(8))
                lines.add(
                    mm.deserialize(
                        "<shadow:#000000:1> <gray>${i + 1}.</gray> <white>$safe...</white>" +
                        " <gray>${stats.totalVotes} votes (streak: ${stats.currentStreak})</gray></shadow>"
                    )
                )
            }
        }

        return Component.join(JoinConfiguration.separator(Component.newline()), lines)
    }

    private fun partyStatus(): Component {
        val state = votePartyService.getState()
        val safeCurrent = mm.escapeTags(state.currentVotes.toString())
        val safeThreshold = mm.escapeTags(state.threshold.toString())
        val remaining = state.threshold - state.currentVotes
        return mm.deserialize(
            if (state.active) {
                "<shadow:#000000:1><gold>Vote Party is <green>ACTIVE</green>!</gold> " +
                "Gold rewards are doubled!</shadow>"
            } else {
                "<shadow:#000000:1><gold>Vote Party Status</gold>: " +
                "<gray>$safeCurrent/$safeThreshold votes ($remaining to go)</gray></shadow>"
            }
        )
    }
}
