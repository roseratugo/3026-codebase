package fr.mma.df.codinggame.api.feature.purchase

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.offre.Offre
import fr.mma.df.codinggame.api.feature.player.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PurchaseRepository : JpaRepository<Purchase, String>