package fr.mma.df.codinggame.api.feature.broadcast.listeners

import fr.mma.df.codinggame.api.feature.broadcast.GameBroadcaster
import fr.mma.df.codinggame.api.feature.treasure.events.TreasureClaimedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class TreasureBroadcastListener(
    private val broadcaster: GameBroadcaster
) {

    @EventListener
    fun onTreasureDiscovered(event: TreasureClaimedEvent) {
        broadcaster.broadcastTreasureDiscovered(event.treasureId)

        // Prévoir un message disant "Un trésor a été découvert sur l'ile .."
    }
}
