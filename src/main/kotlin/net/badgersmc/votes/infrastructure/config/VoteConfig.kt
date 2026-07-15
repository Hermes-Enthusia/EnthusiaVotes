package net.badgersmc.votes.infrastructure.config

data class VoteSite(
    val name: String,
    val url: String,
)

data class VoteConfig(
    val minGold: Int = 1,
    val maxGold: Int = 10,
    val votePartyThreshold: Int = 100,
    val votePartyDurationMinutes: Int = 5,
    val enabledServices: List<String> = emptyList(),
    val voteSites: List<VoteSite> = listOf(
        VoteSite("Planet Minecraft", "https://www.planetminecraft.com/server/badgersmc/vote/"),
        VoteSite("MinecraftServers.org", "https://minecraftservers.org/vote/123456"),
        VoteSite("Minecraft-MP", "https://minecraft-mp.com/server/123456/vote/"),
        VoteSite("TopG", "https://topg.org/minecraft-servers/server-123456/vote"),
    ),
)