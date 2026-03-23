package fr.mma.df.codinggame.api.feature.backoffice

abstract class BoMapper<E, D> {
    abstract fun toDto(entity: E): D
    abstract fun toEntity(dto: D): E

    abstract fun toDto(entities: List<E>): List<D>
    abstract fun toEntity(dtos: List<D>): List<E>

    abstract fun partialUpdate(dto: D, entity: E): E
}