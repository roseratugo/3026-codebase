package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslands
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.offre.Offre
import fr.mma.df.codinggame.api.feature.resource.Resource
import fr.mma.df.codinggame.api.feature.ship.Ship
import fr.mma.df.codinggame.api.feature.error.Error
import fr.mma.df.codinggame.api.feature.storage.Storage
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "player")
class Player(
    @Id
    @UuidGenerator
    var id: String? = null,
    var signUpCode: String? = null,
    @Column(nullable = false)
    var name: String,
    var color: String? = null,
    var discordKey: String? = null,
    var quotient: Int,
    var money: Float,
    var token: String? = null,
    @Enumerated(EnumType.STRING)
    var mainResource: ResourceTypeEnum? = null,

    @ManyToMany
    @JoinTable(
        name = "player_friends",
        joinColumns = [JoinColumn(name = "player_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "friend_id", referencedColumnName = "id")]
    )
    var friends: MutableList<Player>? = mutableListOf(),

    @OneToOne
    @JoinColumn(name = "ship_id", referencedColumnName = "id")
    var ship: Ship? = null,

    @OneToOne
    @JoinColumn(name = "storage_id", referencedColumnName = "id")
    var storage: Storage? = null,

    @OneToMany(mappedBy = "owner")
    var offers: MutableList<Offre>? = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "player", orphanRemoval = true)
    var resources: MutableList<Resource>? = mutableListOf(),

    @OneToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "island_id", referencedColumnName = "id")
    var home: Island? = null,

    @OneToMany(mappedBy = "player")
    val discoveredIslands: MutableList<DiscoveredIslands>? = mutableListOf(),

    @OneToMany(mappedBy = "player")
    val errors: MutableList<Error>? = mutableListOf(),
)