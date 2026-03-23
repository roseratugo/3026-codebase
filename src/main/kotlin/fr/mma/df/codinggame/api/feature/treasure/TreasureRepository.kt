package fr.mma.df.codinggame.api.feature.treasure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface TreasureRepository : JpaRepository<Treasure, String> {

    /**
     * Cherche un Treasure sur une Cell donnée avec son Island (LEFT JOIN FETCH).
     * Utilisé lors de l'exploration d'une cellule pour récupérer le trésor s'il existe.
     * Évite les N+1 queries en chargeant l'island en même temps.
     */
    @Query("SELECT t FROM Treasure t WHERE t.cell.id = :cellId")
    fun findByCellId(cellId: String): Treasure?

    /**
     * Vérifie si un Treasure existe sur une Cell donnée.
     * Requête optimisée (COUNT) pour la validation métier lors de la création.
     */
    @Query("SELECT COUNT(t) > 0 FROM Treasure t WHERE t.cell.id = :cellId")
    fun existsByCell_Id(cellId: String): Boolean

}