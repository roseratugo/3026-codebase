package fr.mma.df.codinggame.api.feature.storageLevel

import fr.mma.df.codinggame.api.feature.storage.Storage
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * Les StorageLevel sont constitués par .ini ou par la BDD
 */
@Entity
@Table(name = "storage_level")
class StorageLevel(
    @Id
    val id: Int,

    var name: String,

    var maxResourcePers: Int,
    var maxResourceA: Int,
    var maxResourceB: Int,

    var costResourcePers: Int,
    var costResourceA: Int,
    var costResourceB: Int,
)