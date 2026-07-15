package net.badgersmc.votes.infrastructure.papi

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.badgersmc.votes.application.VotePartyService
import net.badgersmc.votes.application.VoteRepository
import net.badgersmc.votes.domain.PlayerStats
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class EnthusiaVotesExpansion(
    private val voteRepository: VoteRepository,
    private val votePartyService: VotePartyService,
) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "enthusiavotes"
    override fun getAuthor(): String = "BadgersMC"
    override fun getVersion(): String = "0.1.0"
    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null

        val stats: PlayerStats by lazy { voteRepository.getStats(player.uniqueId) }

        return when (params.lowercase()) {
            "total" -> stats.totalVotes.toString()
            "streak" -> stats.currentStreak.toString()
            "best_streak" -> stats.bestStreak.toString()
            "party_active" -> if (votePartyService.isPartyActive()) "true" else "false"
            "party_votes" -> votePartyService.getCurrentVotes().toString()
            "party_needed" -> votePartyService.getVotesNeeded().toString()
            "party_remaining" -> votePartyService.getRemainingVotes().toString()
            else -> null
        }
    }
}
