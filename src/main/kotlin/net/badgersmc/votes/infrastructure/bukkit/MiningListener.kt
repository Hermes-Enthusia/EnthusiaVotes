package net.badgersmc.votes.infrastructure.bukkit

import net.badgersmc.nexus.i18n.LangService
import net.badgersmc.votes.application.RewardService
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class MiningListener(
    private val rewardService: RewardService,
    @Suppress("unused") private val lang: LangService,
) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val block = event.block
        if (!GOLD_ORES.contains(block.type)) return

        val multiplier = rewardService.getMiningMultiplier(event.player.uniqueId)
        if (multiplier <= 1.0) return

        // Keep vanilla drops (fortune works normally), add extra raw gold on top
        val bonusOres = (multiplier - 1.0).toInt().let { base ->
            val fractional = (multiplier - 1.0) - base
            if (fractional > 0 && Math.random() < fractional) base + 1 else base
        }
        if (bonusOres > 0) {
            block.world.dropItemNaturally(block.location, ItemStack(Material.RAW_GOLD, bonusOres))
        }
    }

    companion object {
        private val GOLD_ORES = setOf(
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.NETHER_GOLD_ORE,
        )
    }
}
