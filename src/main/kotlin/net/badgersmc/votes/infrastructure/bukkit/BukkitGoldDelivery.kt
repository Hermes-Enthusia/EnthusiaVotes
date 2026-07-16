package net.badgersmc.votes.infrastructure.bukkit

import net.badgersmc.votes.application.GoldDelivery
import org.bukkit.Bukkit
import java.util.UUID

class BukkitGoldDelivery : GoldDelivery {
    override fun deliver(playerUuid: UUID, amount: Int): Boolean {
        val player = Bukkit.getPlayer(playerUuid) ?: return false
        VoteItemFactory.giveGold(player, amount)
        return true
    }
}