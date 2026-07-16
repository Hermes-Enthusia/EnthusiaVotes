package net.badgersmc.votes.infrastructure.bukkit

import com.google.gson.Gson
import net.badgersmc.votes.application.VotePartySpeaker
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

/**
 * Handles proxy plugin messaging for VoteParty broadcasts.
 *
 * Registers channel `enthusiavotes:voteparty`.
 * Broadcasts "party_start" and "party_end" messages to the proxy.
 *
 * If no Velocity/Bungee proxy is present, plugin messages are silently ignored.
 */
class ProxiedDeliveryService(
    private val plugin: EnthusiaVotesPlugin,
) : PluginMessageListener, VotePartySpeaker {

    private val gson = Gson()
    private val channelVoteParty = "enthusiavotes:voteparty"

    fun register() {
        val messenger = plugin.server.messenger
        messenger.registerOutgoingPluginChannel(plugin, channelVoteParty)
        messenger.registerIncomingPluginChannel(plugin, channelVoteParty, this)
    }

    fun unregister() {
        val messenger = plugin.server.messenger
        messenger.unregisterIncomingPluginChannel(plugin, channelVoteParty)
        messenger.unregisterOutgoingPluginChannel(plugin, channelVoteParty)
    }

    // ----- VotePartySpeaker -----------------------------------------------

    override fun onPartyActivated() {
        val payload = mapOf("type" to "party_start")
        sendVotePartyMessage(payload)
    }

    override fun onPartyDeactivated() {
        val payload = mapOf("type" to "party_end")
        sendVotePartyMessage(payload)
    }

    private fun sendVotePartyMessage(payload: Map<String, Any>) {
        val bytes = gson.toJson(payload).toByteArray(Charsets.UTF_8)
        plugin.server.sendPluginMessage(plugin, channelVoteParty, bytes)
    }

    // ----- PluginMessageListener ------------------------------------------

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel == channelVoteParty) {
            handleVotePartyMessage(message)
        }
    }

    private fun handleVotePartyMessage(message: ByteArray) {
        // Proxy-side handling only; servers receive the broadcast
        // through the existing VoteBroadcaster path.
        // This hook is reserved for future cross-server state sync.
    }
}