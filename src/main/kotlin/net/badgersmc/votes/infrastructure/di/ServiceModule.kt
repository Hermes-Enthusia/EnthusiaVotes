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

class VoteScheduler(private val plugin: EnthusiaVotesPlugin) {
    fun start() {
        // TODO: schedule vote party reset, streak reset checks
    }

    fun stop() {
        // TODO: cancel tasks
    }
}

class ServiceModule(
    val plugin: EnthusiaVotesPlugin,
) {
    val nexusScheduler = NexusScheduler(plugin)

    val scheduler = VoteScheduler(plugin)

    val databaseFactory: DatabaseFactory
        get() = plugin.databaseFactory ?: error("DatabaseFactory not initialized")

    val voteRepository: VoteRepository by lazy {
        SqliteVoteRepository(databaseFactory)
    }

    val voteConfig: VoteConfig by lazy {
        VoteConfig()
    }

    val rewardService: RewardService by lazy {
        RewardService(voteConfig)
    }

    val voteBroadcaster: VoteBroadcaster by lazy {
        BukkitVoteBroadcaster()
    }

    val goldDelivery: GoldDelivery by lazy {
        BukkitGoldDelivery()
    }

    val voteService: VoteService by lazy {
        VoteService(voteRepository, rewardService, voteBroadcaster, goldDelivery)
    }

    val voteCommand: VoteCommand by lazy { VoteCommand() }
    val voteSitesCommand: VoteSitesCommand by lazy { VoteSitesCommand() }
    val evAdminCommand: EVAdminCommand by lazy { EVAdminCommand() }

    val voteListener: VotifierVoteListener by lazy {
        VotifierVoteListener(voteService)
    }

    val resumeGiveawaysOnStartup: () -> Unit = {
        // TODO: resume vote party state from DB
    }
}
