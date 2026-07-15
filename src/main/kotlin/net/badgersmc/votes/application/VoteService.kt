package net.badgersmc.votes.application

import net.badgersmc.votes.domain.PlayerStats
import net.badgersmc.votes.domain.VoteParty
import net.badgersmc.votes.domain.VoteRecord
import net.kyori.adventure.text.Component
import java.util.UUID

class VoteService(
    private val repo: VoteRepository,
    private val rewardService: RewardService,
    private val broadcaster: VoteBroadcaster,
    private val goldDelivery: GoldDelivery,
) {

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
