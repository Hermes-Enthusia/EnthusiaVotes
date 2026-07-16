package net.badgersmc.votes.infrastructure.form

import net.badgersmc.nexus.i18n.LangService
import net.badgersmc.votes.application.VoteRepository
import net.badgersmc.votes.infrastructure.config.VoteConfig
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.SimpleForm
import java.util.UUID
import java.util.logging.Logger

/** Sends a SimpleForm to Bedrock players for /vote. Uses reflection for FloodgateApi to avoid
 * NoClassDefFoundError when Floodgate is not present or is in a different classloader. */
class BedrockVoteForm(
    private val voteRepository: VoteRepository,
    private val voteConfig: VoteConfig,
    private val logger: Logger,
    private val lang: LangService,
) {
    companion object {
        fun isBedrockPlayer(player: Player): Boolean {
            return try {
                val apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
                val getInstance = apiClass.getMethod("getInstance")
                val api = getInstance.invoke(null)
                val isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID::class.java)
                isFloodgatePlayer.invoke(api, player.uniqueId) as Boolean
            } catch (_: Exception) {
                false
            }
        }
    }

    fun open(player: Player) {
        val stats = voteRepository.getStats(player.uniqueId)
        val form = SimpleForm.builder()
            .title(lang.raw("bedrock.form.title"))
            .content(
                lang.legacy(
                    "bedrock.form.content",
                    "total" to stats.totalVotes.toString(),
                    "streak" to stats.currentStreak.toString(),
                    "best" to stats.bestStreak.toString(),
                )
            )

        for (site in voteConfig.voteSites) {
            form.button(site.name)
        }

        form.closedOrInvalidResultHandler { _, _ -> /* ignore close */ }
        form.validResultHandler { _, response ->
            val index = response.clickedButtonId()
            if (index < voteConfig.voteSites.size) {
                val site = voteConfig.voteSites[index]
                player.sendMessage(lang.msg("bedrock.form.link_message", "name" to site.name, "url" to site.url))
            }
        }

        try {
            val apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
            val getInstance = apiClass.getMethod("getInstance")
            val api = getInstance.invoke(null)
            val sendForm = apiClass.getMethod("sendForm", UUID::class.java, SimpleForm::class.java)
            sendForm.invoke(api, player.uniqueId, form)
        } catch (e: Exception) {
            logger.warning("Failed to open Bedrock vote form for ${player.name}: ${e.message}")
        }
    }
}
