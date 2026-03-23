package fr.mma.df.codinggame.api.feature.resource

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceRepository : JpaRepository<Resource, String> {

}