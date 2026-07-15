package net.badgersmc.votes.infrastructure.bukkit

import net.badgersmc.votes.application.EVAdminCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class EVAdminBukkitCommand(
    private val evAdminCommand: EVAdminCommand,
) : Command("evadmin") {
    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("enthusiavotes.admin")) {
            sender.sendRichMessage("<red>You don't have permission.</red>")
            return true
        }

        val playerUuid = (sender as? Player)?.uniqueId ?: java.util.UUID.randomUUID()
        val message = evAdminCommand.execute(sender.name, playerUuid, args.toList())
        sender.sendMessage(message)
        return true
    }
}
