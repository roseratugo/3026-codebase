package fr.mma.df.codinggame.api.feature.island

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IslandService(
    private val repository: IslandRepository,
    private val mapper: IslandMapper,
) : AbstractBackOfficeService<Island, IslandDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    //BUSTER CALL
    override fun delete(id: String) {
        val islandEntity = repository.findById(id)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'a pas été trouvé.") }

        islandEntity.cells?.forEach { cell -> cell.type = CellTypeEnum.SEA; cell.island = null }
        islandEntity.cells?.clear()
        repository.save(islandEntity)
        repository.deleteById(id)
    }


    fun getIslandZone(islandId: String): Int? {
        return if(repository.getIslandZones(islandId).isNotEmpty()) {
            repository.getIslandZones(islandId)[0]
        } else null
    }


    /*@PostConstruct
    fun defineIslandsZone() {
        repository.findAll().forEach {
            getIslandZone(it.id!!)?.let { zone ->
                it.zone = zone
                logger.info("île : ${it.name} définie dans la zone : ${it.zone}")
                repository.save(it)
            } ?: run {
                logger.info("pas de cellule pour l'ile : ${it.name} - ${it.id}")
            }
        }
    }*/

}
