package fr.mma.df.codinggame.api.feature.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StorageRepository : JpaRepository<Storage, UUID>