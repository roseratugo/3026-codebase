package fr.mma.df.codinggame.api.feature.error

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.player.PlayerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ErrorService(
    private val repository: ErrorRepository,
    private val mapper: ErrorMapper,
    private val playerService: PlayerService
) : AbstractBackOfficeService<Error, ErrorDto, String>(
    repository, mapper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun create(dto: ErrorDto): ErrorDto {
        val player = runCatching {
            playerService.getAuthenticatedPlayerEntity()
        }.getOrNull() // si exception levée on met player = null

        val error = Error(
            code = dto.code.toString(),
            player = player
        )

        val savedError = repository.save(error)
        return mapper.toDto(savedError)
    }
}