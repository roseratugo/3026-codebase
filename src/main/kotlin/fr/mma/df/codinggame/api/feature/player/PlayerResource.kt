package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.exception.UnknownAdminCodeException
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import fr.mma.df.codinggame.api.feature.storage.StorageService
import jakarta.annotation.security.PermitAll
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/players")
open class PlayerResource(
    private val service: PlayerService,
    private val storageService: StorageService,
    @Value("\${admin.account.creation.secret.code}") private val adminSecretCode: String,
) : AbstractBackOfficeResource<Player, PlayerDto, String>(service) {

    @GetMapping("/{id}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    override fun read(@PathVariable id: String): PlayerDto {
        return service.read(id).apply {
            storage = this.storage?.let { storageService.toStorageLiteDtoUserFormat(it) }
        }
    }

    @GetMapping
    @PreAuthorize(CHECK_ADMIN_ROLE)
    override fun readAll(): List<PlayerDto> {
        return service.readAll().map { playerDto ->
            playerDto.apply {
                storage = this.storage?.let { storageService.toStorageLiteDtoUserFormat(it) }
            }
        }
    }

    @PermitAll
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    open fun create(
        @RequestBody player: PlayerRegistrationDTO,
        @RequestHeader("codinggame-signupcode") signUpCode: String
    ) = CodingGameIdDTO(service.create(signUpCode, player, false))


    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    open fun createAdminAccount(
        @RequestBody player: PlayerRegistrationDTO,
        @RequestHeader("codinggame-signupcode") signUpCode: String,
        @RequestHeader("admin-secret-code") adminSecret: String
    ): CodingGameIdDTO {
        if(adminSecret != adminSecretCode) {
            throw UnknownAdminCodeException(ExceptionCodeEnum.VOUS_NE_PASSEREZ_PAS, "Code admin invalide")
        }
        return CodingGameIdDTO(service.create(signUpCode, player, true))
    }

    @GetMapping("/details")
    @PreAuthorize(CHECK_USER_ROLE)
    open fun getPlayer(authentication: Authentication): PlayerDto? {
        val playerId = authentication.principal as String
        val playerEntity = service.getEntity(playerId)
        return service.read(playerId).apply {
            resources.forEach { it.id = null }
            discoveredIslands.forEach {
                it.id = null
                it.player = null
                it.island = it.island?.copy(id = null)
            }
            home = home?.copy(id = null)
            storage = this.storage?.let { storageService.toStorageLiteDtoUserFormat(it) }
        }
    }

    @GetMapping("/me")
    @PreAuthorize(CHECK_USER_ROLE)
    open fun getMyPlayer(authentication: Authentication): ResponseEntity<Map<String, Any>> {
        val playerId = authentication.principal as String
        val roles = authentication.authorities.map { it.authority }

        println(service.computePlayerQuotient())

        return ResponseEntity.ok(mapOf(
            "playerId" to playerId,
            "roles" to roles
        ))
    }

    @PostMapping("/money/credit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun creditMoney(
        @RequestBody player: PlayerDto,
    ) {
        player.id?.let {
            service.creditMoney(idPlayer = it, player.money ?: 0f)
        }.orThrowIfNull(
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Précisez l'id du player."
        )
    }
}