package net.badgersmc.votes.infrastructure.bukkit

import net.badgersmc.votes.infrastructure.di.ServiceModule
import net.badgersmc.votes.infrastructure.persistence.DatabaseFactory
import net.badgersmc.votes.infrastructure.persistence.Migrations
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class EnthusiaVotesPlugin : JavaPlugin() {

    internal var databaseFactory: DatabaseFactory? = null

    lateinit var services: ServiceModule
        private set

    override fun onEnable() {
        saveDefaultConfig()

        val dbFile = config.getString("storage.file") ?: "votes.db"
        databaseFactory = DatabaseFactory(dataFolder, dbFile)
        Migrations.run()

        services = ServiceModule(this)

        server.commandMap.register(
            "vote",
            VoteBukkitCommand(services.voteCommand, services.bedrockVoteForm),
        )

        server.commandMap.register(
            "votesites",
            VoteSitesBukkitCommand(services.voteSitesCommand),
        )

        server.commandMap.register(
            "evadmin",
            EVAdminBukkitCommand(services.evAdminCommand),
        )

        server.pluginManager.registerEvents(services.voteListener, this)

        // Register PlaceholderAPI expansion if PAPI is present
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            services.placeholderExpansion.register()
            logger.info("PlaceholderAPI expansion registered.")
        }

        services.scheduler.start()

        logger.info("EnthusiaVotes enabled.")
    }

    override fun onDisable() {
        if (::services.isInitialized) {
            services.scheduler.stop()
            services.nexusScheduler.cancelAll()
            if (services.placeholderExpansion.isRegistered()) {
                services.placeholderExpansion.unregister()
            }
        }
        databaseFactory?.close()
        databaseFactory = null
    }
}
