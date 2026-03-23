package fr.mma.df.codinggame.api.feature.storage

import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevel
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.*

@Entity
@Table(name = "storage")
class Storage(

    @Id
    @UuidGenerator
    var id: UUID?,

    @OneToOne(mappedBy = "storage")
    var player: Player?,

    @ManyToOne
    @JoinColumn(name = "storage_level_id")
    var level: StorageLevel,
)