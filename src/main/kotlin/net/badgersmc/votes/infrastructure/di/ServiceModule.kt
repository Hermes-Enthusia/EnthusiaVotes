package net.badgersmc.votes.infrastructure.di

import net.badgersmc.nexus.scheduler.NexusScheduler
import net.badgersmc.votes.application.*
import net.badgersmc.votes.infrastructure.bukkit.BukkitGoldDelivery
import net.badgersmc.votes.infrastructure.bukkit.EnthusiaVotesPlugin
import net.badgersmc.votes.infrastructure.bukkit.VotifierVoteListener
import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.badgersmc.votes.infrastructure.messaging.BukkitVoteBroadcaster
import net.badgersmc.votes.infrastructure.persistence.DatabaseFactory
import net.badgersmc.votes.infrastructure.persistence.SqliteVoteRepository
import java.time.Duration

class VoteScheduler(
    private val plugin: EnthusiaVotesPlugin,
    private val votePartyService: VotePartyService,
    private val config: VoteConfig,
) {
    fun start() {
        if (votePartyService.isPartyActive()) {
            val duration = Duration.ofMinutes(config.votePartyDurationMinutes.toLong())
            val ticks = duration.seconds * 20
            plugin.server.scheduler.runTaskLater(
                plugin,
                Runnable { votePartyService.deactivate() },
                ticks,
            )
        }
    }

    fun stop() {
        // Tasks handled by Bukkit scheduler lifecycle
    }
}

class ServiceModule(
    val plugin: EnthusiaVotesPlugin,
) {
    val nexusScheduler = NexusScheduler(plugin)

    val databaseFactory: DatabaseFactory
        get() = plugin.databaseFactory ?: error("DatabaseFactory not initialized")

    val voteRepository: VoteRepository by lazy {
        SqliteVoteRepository(databaseFactory)
    }

    val voteConfig: VoteConfig by lazy {
        VoteConfig()
    }

    val votePartyService: VotePartyService by lazy {
        VotePartyService(voteConfig, plugin)
    }

    val scheduler: VoteScheduler by lazy {
        VoteScheduler(plugin, votePartyService, voteConfig)
    }

    val rewardService: RewardService by lazy {
        RewardService(voteConfig, votePartyService)
    }

    val voteBroadcaster: VoteBroadcaster by lazy {
        BukkitVoteBroadcaster()
    }

    val goldDelivery: GoldDelivery by lazy {
        BukkitGoldDelivery()
    }

    val voteService: VoteService by lazy {
        VoteService(voteRepository, rewardService, voteBroadcaster, goldDelivery, votePartyService, voteConfig)
    }

    val voteCommand: VoteCommand by lazy { VoteCommand(voteRepository, voteConfig) }
    val voteSitesCommand: VoteSitesCommand by lazy { VoteSitesCommand(voteConfig) }
    val evAdminCommand: EVAdminCommand by lazy { EVAdminCommand() }

    val voteListener: VotifierVoteListener by lazy {
        VotifierVoteListener(voteService)
    }

    val resumeGiveawaysOnStartup: () -> Unit = {
        // TODO: resume vote party state from DB
    }
}
