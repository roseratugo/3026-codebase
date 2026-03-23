package fr.mma.df.codinggame.api.feature.playerInsurance

import org.springframework.data.jpa.repository.JpaRepository

interface PlayerInsuranceRepository : JpaRepository<PlayerInsurance, String> {

    // Vérifie si un joueur a déjà souscrit une assurance précise
    fun existsByPlayer_IdAndInsurance_Id(playerId: String, insuranceId: String): Boolean

    // Récupère toutes les assurances d’un joueur
    fun findAllByPlayer_Id(playerId: String): List<PlayerInsurance>

    // Récupère la souscription d’un joueur (utile pour le remboursement)
    fun findByPlayer_Id(playerId: String): PlayerInsurance?

    // Récupère une souscription précise
    fun findByPlayer_IdAndInsurance_Id(playerId: String, insuranceId: String): PlayerInsurance?
}
