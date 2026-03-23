package fr.mma.df.codinggame.api.feature.whitelist

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "whitelist")
class Whitelist (

    @Id
    @UuidGenerator
    val id: String?,
    val mail: String
)
