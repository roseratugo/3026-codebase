package fr.mma.df.codinggame.api.feature.storageLevel

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import org.springframework.stereotype.Service

@Service
class StorageLevelService(

    private val repository: StorageLevelRepository
) {

    /**
     * Charge un niveau d'entrepôt ou rejette une exception métier.
     */
    fun getEntity(level: Int): StorageLevel =
        repository.findById(level)
            .orElseThrow {
                BusinessException(
                    ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT
                )
            }

    /**
     * Méthode générique pour accéder à une propriété d'un StorageLevel.
     */
    open fun <T> getPerLevel(level: Int, accessor: (StorageLevel) -> T): T =
        accessor(getEntity(level))


    // ---- GETTERS ----

    open fun getName(level: Int) =
        getPerLevel(level) { it.name }

    open fun getMaxResourcePers(level: Int) =
        getPerLevel(level) { it.maxResourcePers }

    open fun getMaxResourceA(level: Int) =
        getPerLevel(level) { it.maxResourceA }

    open fun getMaxResourceB(level: Int) =
        getPerLevel(level) { it.maxResourceB }

    open fun getCostResourcePers(level: Int) =
        getPerLevel(level) { it.costResourcePers }

    open fun getCostResourceA(level: Int) =
        getPerLevel(level) { it.costResourceA }

    open fun getCostResourceB(level: Int) =
        getPerLevel(level) { it.costResourceB }

}