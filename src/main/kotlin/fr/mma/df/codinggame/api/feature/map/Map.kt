package fr.mma.df.codinggame.api.feature.map

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(name = "mapconfig", uniqueConstraints = [UniqueConstraint(columnNames = ["id"])])
class Map(
    @Id
    val id: String = "key", // id fixe pour avoir une seule entrée de l'entité (<=> Singleton)
    var width: Int,
    var height: Int
)