package net.badgersmc.votes.infrastructure.form

import net.badgersmc.votes.application.VoteRepository
import net.badgersmc.votes.infrastructure.config.VoteConfig
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.SimpleForm
import org.geysermc.floodgate.api.FloodgateApi
import java.util.logging.Logger

/** Sends a SimpleForm to Bedrock players for /vote. */
class BedrockVoteForm(
    private val voteRepository: VoteRepository,
    private val voteConfig: VoteConfig,
    private val logger: Logger,
) {
    companion object {
        fun isBedrockPlayer(player: Player): Boolean {
            return try {
                FloodgateApi.getInstance().isFloodgatePlayer(player.uniqueId)
            } catch (_: Exception) {
                false
            }
        }
    }

    fun open(player: Player) {
        val stats = voteRepository.getStats(player.uniqueId)
        val form = SimpleForm.builder()
            .title("Vote for EnthusiaSMP!")
            .content(
                "Your Stats:\n" +
                "  Total Votes: ${stats.totalVotes}\n" +
                "  Current Streak: ${stats.currentStreak}\n" +
                "  Best Streak: ${stats.bestStreak}\n\n" +
                "Click a site below to vote!"
            )

        for (site in voteConfig.voteSites) {
            form.button(site.name)
        }

        form.closedOrInvalidResultHandler { _, _ -> /* ignore close */ }
        form.validResultHandler { _, response ->
            val index = response.clickedButtonId()
            if (index < voteConfig.voteSites.size) {
                val site = voteConfig.voteSites[index]
                player.sendRichMessage(
                    "<shadow:#000000:1><gold>Vote link:</gold> " +
                    "<click:open_url:'${site.url}'><aqua>${site.name}</aqua></click></shadow>"
                )
            }
        }

        try {
            FloodgateApi.getInstance().sendForm(player.uniqueId, form)
        } catch (e: Exception) {
            logger.warning("Failed to open Bedrock vote form for ${player.name}: ${e.message}")
        }
    }
}
