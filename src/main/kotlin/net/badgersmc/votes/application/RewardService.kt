package net.badgersmc.votes.application

import net.badgersmc.nexus.i18n.LangService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class RewardService(
    private val voteRepository: VoteRepository,
    private val votePartyService: VotePartyService,
    private val lang: LangService,
) {
    companion object {
        /** How long the mining multiplier lasts after activation (millis). */
        const val MULTIPLIER_DURATION_MS: Long = 20 * 60 * 1000L  // 20 minutes
    }

    /**
     * Called after a vote that increased the streak.
     * If the new streak crosses a threshold, records the activation time so the
     * multiplier is active for [MULTIPLIER_DURATION_MS].
     */
    fun tryActivateMultiplier(uuid: UUID, newStreak: Int, oldStreak: Int): Boolean {
        val oldTier = streakMultiplierTier(oldStreak)
        val newTier = streakMultiplierTier(newStreak)
        if (newTier > oldTier) {
            voteRepository.activateMultiplier(uuid)
            return true
        }
        return false
    }

    /**
     * Returns the current mining multiplier. Only active if [MULTIPLIER_DURATION_MS]
     * hasn't elapsed since the last activation. VoteParty multiplier always stacks.
     */
    fun getMiningMultiplier(uuid: UUID): Double {
        val stats = voteRepository.getStats(uuid)
        val elapsed = System.currentTimeMillis() - stats.multiplierActivatedAt
        val streakMult = if (stats.multiplierActivatedAt > 0 && elapsed < MULTIPLIER_DURATION_MS) {
            streakMultiplier(stats.currentStreak)
        } else {
            1.0
        }
        return streakMult * votePartyService.getCurrentMultiplier()
    }

    fun streakMultiplier(streak: Int): Double = when {
        streak >= 30 -> 3.0
        streak >= 7  -> 2.0
        streak >= 3  -> 1.5
        else         -> 1.0
    }

    private fun streakMultiplierTier(streak: Int): Int = when {
        streak >= 30 -> 3
        streak >= 7  -> 2
        streak >= 3  -> 1
        else         -> 0
    }

    fun buildVoteMessage(
        playerName: String,
        gold: Int,
        multiplier: Double,
        streak: Int,
        serviceName: String,
    ): Component {
        val streakStr: String = if (streak > 1)
            MiniMessage.miniMessage().serialize(lang.msg("voteparty.streak_suffix", "streak" to streak.toString()))
        else ""
        return lang.msg(
            "voteparty.reward_message",
            "player" to playerName,
            "service" to serviceName,
            "multiplier" to multiplier.toString(),
            "streak_text" to streakStr,
        )
    }
}
