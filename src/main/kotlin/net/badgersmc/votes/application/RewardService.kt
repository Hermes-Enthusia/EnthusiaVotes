package net.badgersmc.votes.application

import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class RewardService(
    private val config: VoteConfig,
    private val votePartyService: VotePartyService,
) {
    private val mm = MiniMessage.miniMessage()

    fun calculateGold(streak: Int): Int {
        val base = (config.minGold..config.maxGold).random()
        val streakMult = streakMultiplier(streak)
        val partyMult = votePartyService.getCurrentMultiplier()
        return (base * streakMult * partyMult).toInt().coerceAtLeast(1)
    }

    fun streakMultiplier(streak: Int): Double = when {
        streak >= 30 -> 3.0
        streak >= 7 -> 2.0
        streak >= 3 -> 1.5
        else -> 1.0
    }

    fun buildVoteMessage(
        playerName: String,
        gold: Int,
        streak: Int,
        serviceName: String,
    ): Component {
        val safeName = mm.escapeTags(playerName)
        val safeService = mm.escapeTags(serviceName)
        val streakText = if (streak > 1) " (${streak}x streak!)" else ""
        return mm.deserialize(
            "<shadow:#000000:1><yellow>$safeName</yellow> voted on <aqua>$safeService</aqua>" +
            " and received <gold>$gold Raw Gold</gold>$streakText!</shadow>"
        )
    }
}
