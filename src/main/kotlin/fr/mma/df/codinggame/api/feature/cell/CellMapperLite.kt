package fr.mma.df.codinggame.api.feature.cell

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
abstract class CellMapperLite {

    abstract fun toEntity(cellLiteDto: CellLiteDto): Cell

    abstract fun toLiteDto(cell: Cell): CellLiteDto

    abstract fun dtoTOLiteDto(cellDto: CellDto): CellLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(cellLiteDto: CellLiteDto, @MappingTarget cell: Cell): Cell
}