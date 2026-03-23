package fr.mma.df.codinggame.api.feature.backoffice

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable

abstract class AbstractBackOfficeService<E, D, ID : Serializable>(
    private val repository: JpaRepository<E, ID>,
    private val mapper: BoMapper<E, D>
) {

    open fun readAll(): List<D> = mapper.toDto(repository.findAll())

    open fun read(id: ID): D = mapper.toDto(
        repository.findById(id)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'a pas été trouvé.") }
    )

    open fun create(dto: D): D {
        val entity = mapper.toEntity(dto)
        repository.save(entity)
        return mapper.toDto(entity)
    }

    open fun update(id: ID, dto: D): D {
        val entity = repository.findById(id).orElseThrow {
            BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'existe pas")
        }
        val updatedEntity = mapper.partialUpdate(dto, entity)
        return mapper.toDto(repository.save(updatedEntity))
    }

    open fun delete(id: ID) = repository.deleteById(id)

    open fun getEntity(id: ID): E =
        repository.findById(id).orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'existe pas") }
}