package fr.mma.df.codinggame.api.feature.cellstate

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CellStateMapperLite {

    abstract fun toEntity(cellStateLiteDto: CellStateLiteDto): CellState

    abstract fun toDto(cellState: CellState): CellStateLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(cellStateLiteDto: CellStateLiteDto, @MappingTarget cellState: CellState): CellState
}