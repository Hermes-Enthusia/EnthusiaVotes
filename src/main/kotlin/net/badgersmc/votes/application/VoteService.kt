package net.badgersmc.votes.application

import net.badgersmc.votes.domain.PlayerStats
import net.badgersmc.votes.domain.VoteParty
import net.badgersmc.votes.domain.VoteRecord
import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class VoteService(
    private val repo: VoteRepository,
    private val rewardService: RewardService,
    private val broadcaster: VoteBroadcaster,
    private val goldDelivery: GoldDelivery,
    private val votePartyService: VotePartyService,
    private val config: VoteConfig,
) {
    private val mm = MiniMessage.miniMessage()

    fun processVote(playerName: String, playerUuid: UUID, serviceName: String): VoteResult {
        val stats = repo.getStats(playerUuid)
        val streak = stats.currentStreak + 1

        val gold = rewardService.calculateGold(streak)
        val record = VoteRecord(
            playerUuid = playerUuid,
            playerName = playerName,
            serviceName = serviceName,
            goldAwarded = gold,
        )
        repo.saveVote(record)

        goldDelivery.deliver(playerUuid, gold)

        // VoteParty: check if party just activated
        val partyState = votePartyService.onVote()
        if (partyState.justActivated) {
            val partyMsg = mm.deserialize(
                "<shadow:#000000:1><gradient:gold:yellow>VOTE PARTY!</gradient> " +
                "Gold rewards are now <gold>doubled</gold> for " +
                "<aqua>${config.votePartyDurationMinutes} minutes</aqua>!</shadow>"
            )
            broadcaster.broadcastVoteParty(partyMsg)
        }

        val message = rewardService.buildVoteMessage(playerName, gold, streak, serviceName)
        broadcaster.broadcastVote(message)

        return VoteResult(
            record = record,
            stats = stats,
            gold = gold,
            streak = streak,
            broadcastMessage = message,
        )
    }
}

data class VoteResult(
    val record: VoteRecord,
    val stats: PlayerStats,
    val gold: Int,
    val streak: Int,
    val broadcastMessage: Component,
)
