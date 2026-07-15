package net.badgersmc.votes.infrastructure.bukkit

import net.badgersmc.votes.application.VoteCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VoteBukkitCommand(
    private val voteCommand: VoteCommand,
) : Command("vote") {
    override fun execute(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendRichMessage("<red>Only players can use this command.</red>")
            return true
        }

        val message = voteCommand.execute(sender.name, sender.uniqueId)
        sender.sendMessage(message)
        return true
    }
}