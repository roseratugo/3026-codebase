package fr.mma.df.codinggame.api.feature.playerBo

import fr.mma.df.codinggame.api.feature.island.IslandService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/boPlayers")
open class PlayerBoResource(
    private val playerBoService: PlayerBoService,
    private val islandService: IslandService
) {

    /**
     * GET /boPlayers?playerId=xxx&limit=10
     * - si playerId est présent → retourne UN PlayerBO
     * - sinon → retourne la liste complète
     */
    @GetMapping
    fun getPlayerBOs(
        @RequestParam(required = false) playerId: String?,
        @RequestParam(required = false, defaultValue = "1") limit: Int
    ): Any {
        return if (playerId == null) {
            // liste des players
            playerBoService.listPlayerBO(limit)
        } else {
            // un seul player
            playerBoService.getPlayerBO(playerId, limit)
        }
    }

    @GetMapping("/ranking")
    fun getPlayerRanking(): List<RankedPlayer> {
        return playerBoService.listRankedPlayers()
    }

}