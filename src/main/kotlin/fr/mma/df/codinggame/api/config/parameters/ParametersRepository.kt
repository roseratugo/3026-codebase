package fr.mma.df.codinggame.api.config.parameters

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParametersRepository : JpaRepository<Parameters, Long> {

}