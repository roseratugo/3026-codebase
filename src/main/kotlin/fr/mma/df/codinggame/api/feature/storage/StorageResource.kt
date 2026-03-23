package fr.mma.df.codinggame.api.feature.storage

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevelDto
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/storage")
class StorageResource(
    private val storageService: StorageService
) {

    @PreAuthorize(CHECK_USER_ROLE)
    @GetMapping
    fun read(): ResponseEntity<StorageDto> {
        return ResponseEntity.ok(storageService.read())
    }

    @PreAuthorize(CHECK_USER_ROLE)
    @GetMapping("/next-level")
    fun readNextLevel(): ResponseEntity<StorageLevelDto> {
        return ResponseEntity.ok(storageService.readNextLevel())
    }

    @PreAuthorize(CHECK_ADMIN_ROLE)
    @PostMapping
    fun create(): ResponseEntity<UUID> {
        return ResponseEntity.ok(storageService.create())
    }

    @PreAuthorize(CHECK_USER_ROLE)
    @GetMapping("/can-upgrade")
    fun canUpgrade(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(storageService.canUpgrade())
    }

    @PreAuthorize(CHECK_USER_ROLE)
    @PutMapping("/upgrade")
    fun upgrade(): StorageLiteDto {
        return storageService.upgrade()
    }
}