package net.badgersmc.votes.application

import java.util.UUID

interface GoldDelivery {
    /** Gives RAW_GOLD to a player. Returns true if delivered successfully. */
    fun deliver(playerUuid: UUID, amount: Int): Boolean
}
