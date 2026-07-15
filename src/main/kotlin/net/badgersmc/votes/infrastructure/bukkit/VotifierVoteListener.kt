package net.badgersmc.votes.infrastructure.bukkit

import com.vexsoftware.votifier.model.VotifierEvent
import net.badgersmc.votes.application.VoteService
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VotifierVoteListener(
    private val voteService: VoteService,
) : Listener {

    @EventHandler
    fun onVote(event: VotifierEvent) {
        val vote = event.vote
        val playerName = vote.username
        val serviceName = vote.serviceName
        val playerUuid = Bukkit.getOfflinePlayer(playerName).uniqueId

        voteService.processVote(playerName, playerUuid, serviceName)
    }
}
