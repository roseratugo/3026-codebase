package fr.mma.df.codinggame.api.feature.positionhistory

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class PositionHistoryMapperLite {

    abstract fun toEntity(positionHistoryLiteDto: PositionHistoryLiteDto): PositionHistory

    abstract fun toDto(positionHistory: PositionHistory): PositionHistoryLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(
        positionHistoryLiteDto: PositionHistoryLiteDto,
        @MappingTarget positionHistory: PositionHistory
    ): PositionHistory
}