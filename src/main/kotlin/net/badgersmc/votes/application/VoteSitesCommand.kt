package net.badgersmc.votes.application

import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.minimessage.MiniMessage

class VoteSitesCommand(
    private val voteConfig: VoteConfig,
) {
    private val mm = MiniMessage.miniMessage()

    fun execute(): Component {
        val lines = mutableListOf(
            mm.deserialize(
                "<shadow:#000000:1><gold>▶</gold> <yellow>Vote for us!</yellow></shadow>"
            ),
        )

        for (site in voteConfig.voteSites) {
            val safeName = mm.escapeTags(site.name)
            val safeUrl = mm.escapeTags(site.url)
            lines.add(
                mm.deserialize(
                    "<shadow:#000000:1> <gold>▶</gold> <aqua>$safeName</aqua> <gray>- " +
                    "<click:open_url:'$safeUrl'>Click to vote!</click></gray></shadow>"
                )
            )
        }

        return Component.join(newline(), lines)
    }
}