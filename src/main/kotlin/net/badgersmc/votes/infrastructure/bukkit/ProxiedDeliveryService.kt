package net.badgersmc.votes.infrastructure.bukkit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.badgersmc.votes.application.GoldDelivery
import net.badgersmc.votes.application.VotePartySpeaker
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.UUID

/**
 * Handles proxy plugin messaging for gold delivery and VoteParty broadcasts.
 *
 * **Gold delivery** – registers channel `enthusiavotes:deliver`.
 * When a player is online locally, gold is delivered directly.
 * When they are not, a plugin message is sent to the proxy, which forwards
 * it to the server where the player is connected.
 *
 * **VoteParty** – registers channel `enthusiavotes:voteparty`.
 * Broadcasts "party_start" and "party_end" messages to the proxy.
 *
 * If no Velocity/Bungee proxy is present, plugin messages are silently ignored.
 */
class ProxiedDeliveryService(
    private val plugin: EnthusiaVotesPlugin,
    private val localDelivery: BukkitGoldDelivery,
) : GoldDelivery, PluginMessageListener, VotePartySpeaker {

    private val gson = Gson()
    private val channelGold = "enthusiavotes:deliver"
    private val channelVoteParty = "enthusiavotes:voteparty"

    fun register() {
        val messenger = plugin.server.messenger
        messenger.registerOutgoingPluginChannel(plugin, channelGold)
        messenger.registerIncomingPluginChannel(plugin, channelGold, this)
        messenger.registerOutgoingPluginChannel(plugin, channelVoteParty)
        messenger.registerIncomingPluginChannel(plugin, channelVoteParty, this)
    }

    fun unregister() {
        val messenger = plugin.server.messenger
        messenger.unregisterIncomingPluginChannel(plugin, channelGold)
        messenger.unregisterOutgoingPluginChannel(plugin, channelGold)
        messenger.unregisterIncomingPluginChannel(plugin, channelVoteParty)
        messenger.unregisterOutgoingPluginChannel(plugin, channelVoteParty)
    }

    // ----- GoldDelivery ---------------------------------------------------

    override fun deliver(playerUuid: UUID, amount: Int): Boolean {
        val player = Bukkit.getPlayer(playerUuid)
        if (player != null) {
            return localDelivery.deliver(playerUuid, amount)
        }

        // Player not on this server — try proxy forwarding
        val payload = mapOf(
            "type" to "gold",
            "playerUuid" to playerUuid.toString(),
            "playerName" to "",
            "gold" to amount,
        )
        val bytes = gson.toJson(payload).toByteArray(Charsets.UTF_8)
        plugin.server.sendPluginMessage(plugin, channelGold, bytes)
        return true // assumed delivered via proxy
    }

    // ----- PluginMessageListener ------------------------------------------

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        when (channel) {
            channelGold -> handleGoldMessage(message)
            channelVoteParty -> handleVotePartyMessage(message)
        }
    }

    private fun handleGoldMessage(message: ByteArray) {
        try {
            val json = String(message, Charsets.UTF_8)
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, mapType)

            if (data["type"] != "gold") return

            val uuidStr = data["playerUuid"] as? String ?: return
            val goldVal = (data["gold"] as? Number)?.toInt() ?: return
            val uuid = UUID.fromString(uuidStr)

            localDelivery.deliver(uuid, goldVal)
        } catch (_: Exception) {
            plugin.logger.warning("Failed to parse gold plugin message")
        }
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

    private fun handleVotePartyMessage(message: ByteArray) {
        // Proxy-side handling only; servers receive the broadcast
        // through the existing VoteBroadcaster path.
        // This hook is reserved for future cross-server state sync.
    }
}