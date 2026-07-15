package net.badgersmc.votes.infrastructure.bukkit

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/** Creates RAW_GOLD items with Unbreaking 1 + HIDE_ENCHANTS (currency glint). */
object VoteItemFactory {

    /** Builds a RAW_GOLD item with optional custom name and amount. */
    fun rawGold(amount: Int = 1, name: Component? = null): ItemStack {
        val item = ItemStack(Material.RAW_GOLD, amount)
        val meta = item.itemMeta ?: return item
        if (name != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false))
        }
        meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = meta
        return item
    }

    /** Gives RAW_GOLD to a player, dropping excess on the ground if inventory is full. */
    fun giveGold(player: Player, amount: Int) {
        val item = rawGold(amount)
        val leftover = player.inventory.addItem(item)
        for (left in leftover.values) {
            player.world.dropItemNaturally(player.location, left)
        }
    }
}
