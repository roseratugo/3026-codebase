package fr.mma.df.codinggame.api.feature.whitelist

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
)
abstract class WhitelistMapper : BoMapper<Whitelist, WhitelistDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: WhitelistDto, @MappingTarget entity: Whitelist): Whitelist

}