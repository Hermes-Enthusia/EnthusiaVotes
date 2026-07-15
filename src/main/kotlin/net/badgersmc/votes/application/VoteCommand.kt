package net.badgersmc.votes.application

import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class VoteCommand(
    private val voteRepository: VoteRepository,
    private val voteConfig: VoteConfig,
) {
    private val mm = MiniMessage.miniMessage()

    fun execute(playerName: String, playerUuid: UUID): Component {
        val safeName = mm.escapeTags(playerName)
        val stats = voteRepository.getStats(playerUuid)

        val safeTotal = mm.escapeTags(stats.totalVotes.toString())
        val safeStreak = mm.escapeTags(stats.currentStreak.toString())
        val safeBest = mm.escapeTags(stats.bestStreak.toString())

        val lines = mutableListOf(
            mm.deserialize(
                "<shadow:#000000:1><gold>▶</gold> <yellow>Your Vote Stats, $safeName</yellow></shadow>"
            ),
            mm.deserialize(
                "<shadow:#000000:1> <gray>Total Votes:</gray> <white>$safeTotal</white></shadow>"
            ),
            mm.deserialize(
                "<shadow:#000000:1> <gray>Current Streak:</gray> <white>$safeStreak</white></shadow>"
            ),
            mm.deserialize(
                "<shadow:#000000:1> <gray>Best Streak:</gray> <white>$safeBest</white></shadow>"
            ),
            mm.deserialize(
                "<shadow:#000000:1></shadow>"
            ),
            mm.deserialize(
                "<shadow:#000000:1><gold>▶</gold> <yellow>Vote Sites</yellow></shadow>"
            ),
        )

        for (site in voteConfig.voteSites) {
            val safeNameS = mm.escapeTags(site.name)
            val safeUrl = mm.escapeTags(site.url)
            lines.add(
                mm.deserialize(
                    "<shadow:#000000:1> <gold>▶</gold> <aqua>$safeNameS</aqua> <gray>- " +
                    "<click:open_url:'$safeUrl'>Click to vote!</click></gray></shadow>"
                )
            )
        }

        return Component.join(newline(), lines)
    }
}