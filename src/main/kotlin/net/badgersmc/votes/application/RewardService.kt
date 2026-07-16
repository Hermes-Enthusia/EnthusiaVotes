package net.badgersmc.votes.application

import net.badgersmc.nexus.i18n.LangService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RewardService(
    private val voteRepository: VoteRepository,
    private val votePartyService: VotePartyService,
    private val lang: LangService,
) {
    private val multiplierCache = ConcurrentHashMap<UUID, Double>()

    fun cacheMultiplier(uuid: UUID, streak: Int) {
        val partyMult = votePartyService.getCurrentMultiplier()
        val streakMult = streakMultiplier(streak)
        multiplierCache[uuid] = streakMult * partyMult
    }

    fun getMiningMultiplier(uuid: UUID): Double {
        multiplierCache[uuid]?.let { return it }
        val stats = voteRepository.getStats(uuid)
        val streak = stats.currentStreak
        val streakMult = streakMultiplier(streak)
        val partyMult = votePartyService.getCurrentMultiplier()
        return (streakMult * partyMult).also { multiplierCache[uuid] = it }
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
        multiplier: Double,
        streak: Int,
        serviceName: String,
    ): Component {
        // Serialize streak suffix to MiniMessage string before inserting into template —
        // passing a Component directly breaks the parser (shows raw TextComponentImpl dump)
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
