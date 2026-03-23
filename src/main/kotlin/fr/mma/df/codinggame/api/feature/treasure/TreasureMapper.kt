package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING
)
abstract class TreasureMapper : BoMapper<Treasure, TreasureDto>() {

    /**
     * Convertit un TreasureCreateRequest vers l'entité Treasure.
     * La Cell doit être chargée en service, le mapper ignore ce champ.
     */
    @Mapping(target = "cell", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "claimed", constant = "false")
    abstract fun createRequestToEntity(request: TreasureCreateRequest): Treasure


    /**
     * Convertit une liste de TreasureCreateRequest vers une liste d'entités Treasure.
     */
    fun createRequestsToEntities(requests: List<TreasureCreateRequest>): List<Treasure> {
        return requests.map { createRequestToEntity(it) }
    }

    @Mapping(target = "cell", expression = "java(cellToDto(treasure.getCell()))")
    abstract override fun toDto(treasure: Treasure): TreasureDto

    /**
     * Mise à jour partielle (PATCH) d'une entité Treasure à partir d'un DTO.
     * Les champs null dans le DTO sont ignorés.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cell", ignore = true)
    abstract override fun partialUpdate(dto: TreasureDto, @MappingTarget entity: Treasure): Treasure

    protected fun cellToDto(cell: fr.mma.df.codinggame.api.feature.cell.Cell?): TreasureCellDto? {
        if (cell == null) return null
        return TreasureCellDto(
            id = cell.id,
            x = cell.x,
            y = cell.y,
            zone = cell.zone,
            islandId = cell.island?.id,
            islandName = cell.island?.name,
        )
    }
}