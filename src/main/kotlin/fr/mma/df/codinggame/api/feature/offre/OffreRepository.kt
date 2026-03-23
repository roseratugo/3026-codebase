package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OffreRepository : JpaRepository<Offre, String> {

    //methode pour savoir si une offre existe indépendament de son id
    fun existsByOwnerAndResourceType(owner: Player, resourceType: ResourceTypeEnum): Boolean

    //on veut savoir si c'est bien l'owner de cette offre
    fun existsByIdAndOwnerId(id: String, owner: String): Boolean


}