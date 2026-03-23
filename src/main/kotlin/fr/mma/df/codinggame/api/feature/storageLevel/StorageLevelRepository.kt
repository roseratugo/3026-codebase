package fr.mma.df.codinggame.api.feature.storageLevel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StorageLevelRepository : JpaRepository<StorageLevel, Int> {}