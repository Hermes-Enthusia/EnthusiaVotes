package net.badgersmc.votes.infrastructure.di

import net.badgersmc.nexus.i18n.LangService
import net.badgersmc.nexus.i18n.Locale
import net.badgersmc.nexus.scheduler.NexusScheduler
import net.badgersmc.votes.application.*
import net.badgersmc.votes.infrastructure.bukkit.BukkitGoldDelivery
import net.badgersmc.votes.infrastructure.bukkit.EnthusiaVotesPlugin
import net.badgersmc.votes.infrastructure.bukkit.MiningListener
import net.badgersmc.votes.infrastructure.bukkit.OfflineVoteLoginListener
import net.badgersmc.votes.infrastructure.bukkit.ProxiedDeliveryService
import net.badgersmc.votes.infrastructure.bukkit.VoteReminder
import net.badgersmc.votes.infrastructure.bukkit.VotifierVoteListener
import net.badgersmc.votes.infrastructure.config.VoteConfig
import net.badgersmc.votes.infrastructure.config.MariaDbConfig
import net.badgersmc.votes.infrastructure.config.StorageConfig
import net.badgersmc.votes.infrastructure.config.VoteSite
import net.badgersmc.votes.infrastructure.config.VoteSitePresets
import net.badgersmc.votes.infrastructure.form.BedrockVoteForm
import net.badgersmc.votes.infrastructure.i18n.EnthusiaVotesLang
import net.badgersmc.votes.infrastructure.messaging.BukkitVoteBroadcaster
import net.badgersmc.votes.infrastructure.papi.EnthusiaVotesExpansion
import net.badgersmc.votes.infrastructure.persistence.DatabaseFactory
import net.badgersmc.votes.infrastructure.persistence.SqliteVoteRepository
import java.time.Duration

class VoteScheduler(
    private val plugin: EnthusiaVotesPlugin,
    private val votePartyService: VotePartyService,
    private val config: VoteConfig,
    private val voteReminder: VoteReminder,
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
        // Start repeating reminder from config
        val reminderTicks = 20L * 60 * config.reminderIntervalMinutes
        voteReminder.taskId = plugin.server.scheduler.runTaskTimer(
            plugin,
            voteReminder,
            reminderTicks,
            reminderTicks,
        ).taskId
    }

    fun stop() {
        if (voteReminder.taskId != -1) {
            plugin.server.scheduler.cancelTask(voteReminder.taskId)
        }
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
        loadConfig()
    }

    private fun loadConfig(): VoteConfig {
        val configFile = java.io.File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.dataFolder.mkdirs()
            plugin.saveResource("config.yml", false)
        }

        try {
            val yaml = org.yaml.snakeyaml.Yaml()
            val data: Map<String, Any> = yaml.load(configFile.reader())
            val storage = (data["storage"] as? Map<*, *>)?.let { s ->
                val mariadb = (s["mariadb"] as? Map<*, *>)?.let { m ->
                    MariaDbConfig(
                        host = m["host"]?.toString() ?: "localhost",
                        port = (m["port"] as? Number)?.toInt() ?: 3306,
                        database = m["database"]?.toString() ?: "enthusiavotes",
                        user = m["user"]?.toString() ?: "enthusia",
                        password = m["password"]?.toString() ?: "changeme",
                    )
                } ?: MariaDbConfig()
                StorageConfig(
                    backend = s["backend"]?.toString() ?: "sqlite",
                    file = s["file"]?.toString() ?: "votes.db",
                    mariadb = mariadb,
                )
            } ?: StorageConfig()

            val gold = data["gold"] as? Map<*, *>
            val vp = data["vote-party"] as? Map<*, *>
            val sites = (data["vote-sites"] as? List<*>)?.mapNotNull { s ->
                val site = s as? Map<*, *> ?: return@mapNotNull null
                val name = site["name"]?.toString() ?: "Unknown"
                val url = site["url"]?.toString() ?: ""
                val svcName = (site["service-name"] as? String)?.takeIf { it.isNotBlank() }
                    ?: VoteSitePresets.resolve(url)
                VoteSite(name = name, url = url, serviceName = svcName)
            } ?: emptyList()

            return VoteConfig(
                minGold = (gold?.get("min") as? Number)?.toInt() ?: 1,
                maxGold = (gold?.get("max") as? Number)?.toInt() ?: 10,
                votePartyThreshold = (vp?.get("threshold") as? Number)?.toInt() ?: 100,
                votePartyDurationMinutes = (vp?.get("duration-minutes") as? Number)?.toInt() ?: 5,
                reminderIntervalMinutes = ((data["reminder-interval-minutes"] ?: data["reminderIntervalMinutes"]) as? Number)?.toInt() ?: 5,
                voteSound = data["vote-sound"]?.toString() ?: "BLOCK_AMETHYST_BLOCK_CHIME",
                allSitesSound = data["all-sites-sound"]?.toString() ?: "ENTITY_PLAYER_LEVELUP",
                allSitesBonusGold = ((data["all-sites-bonus-gold"] ?: data["allSitesBonusGold"]) as? Number)?.toInt() ?: 20,
                allSitesBonusMultiplier = ((data["all-sites-bonus-multiplier"] ?: data["allSitesBonusMultiplier"]) as? Number)?.toDouble() ?: 0.5,
                storageConfig = storage,
                voteSites = sites,
            )
        } catch (e: Exception) {
            plugin.logger.warning("Failed to load config.yml: ${e.message}, using defaults")
            return VoteConfig()
        }
    }

    val lang: LangService by lazy {
        LangService(plugin, Locale("en_US"), EnthusiaVotesLang::class.java)
    }

    val votePartySpeaker: VotePartySpeaker by lazy {
        plugin.proxiedDeliveryService ?: NoOpVotePartySpeaker()
    }

    val votePartyService: VotePartyService by lazy {
        VotePartyService(voteConfig, plugin, voteRepository, votePartySpeaker)
    }

    val voteReminder: VoteReminder by lazy {
        VoteReminder(voteRepository, lang, plugin)
    }

    val scheduler: VoteScheduler by lazy {
        VoteScheduler(plugin, votePartyService, voteConfig, voteReminder)
    }

    val rewardService: RewardService by lazy {
        RewardService(voteRepository, votePartyService, lang)
    }

    val voteBroadcaster: VoteBroadcaster by lazy {
        BukkitVoteBroadcaster()
    }

    val goldDelivery: GoldDelivery by lazy {
        BukkitGoldDelivery()
    }

    val voteService: VoteService by lazy {
        VoteService(voteRepository, rewardService, voteBroadcaster, goldDelivery, votePartyService, voteConfig, lang)
    }

    val bedrockVoteForm: BedrockVoteForm by lazy {
        BedrockVoteForm(voteRepository, voteConfig, plugin.logger, lang)
    }

    val voteCommand: VoteCommand by lazy { VoteCommand(voteRepository, voteConfig, lang, rewardService) }
    val voteSitesCommand: VoteSitesCommand by lazy { VoteSitesCommand(voteRepository, voteConfig, lang) }
    val voteTopCommand: VoteTopCommand by lazy { VoteTopCommand(voteRepository, lang) }
    val evAdminCommand: EVAdminCommand by lazy { EVAdminCommand(votePartyService, voteRepository, lang) }

    val voteListener: VotifierVoteListener by lazy {
        VotifierVoteListener(voteService)
    }

    val miningListener: MiningListener by lazy {
        MiningListener(rewardService, lang)
    }

    val offlineVoteLoginListener: OfflineVoteLoginListener by lazy {
        OfflineVoteLoginListener(voteRepository, goldDelivery, lang, rewardService)
    }

    val placeholderExpansion: EnthusiaVotesExpansion by lazy {
        EnthusiaVotesExpansion(voteRepository, votePartyService)
    }

    fun resumeGiveawaysOnStartup() {
        val state = voteRepository.loadPartyState() ?: return
        if (!state.active) return

        votePartyService.loadFrom(state)

        // Reschedule deactivation based on elapsed time
        val startedAt = state.startedAt ?: return
        val elapsed = java.time.Duration.between(startedAt, java.time.Instant.now())
        val totalDuration = Duration.ofMinutes(voteConfig.votePartyDurationMinutes.toLong())
        val remaining = totalDuration.minus(elapsed)
        if (remaining.isPositive) {
            plugin.server.scheduler.runTaskLater(
                plugin,
                Runnable { votePartyService.deactivate() },
                remaining.seconds * 20,
            )
        } else {
            votePartyService.deactivate()
        }
    }
}

private class NoOpVotePartySpeaker : VotePartySpeaker {
    override fun onPartyActivated() {}
    override fun onPartyDeactivated() {}
}