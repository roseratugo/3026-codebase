package fr.mma.df.codinggame.api.feature.insurance

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import org.springframework.stereotype.Service

@Service
open class InsuranceService(
    private val repository: InsuranceRepository,
    private val mapper: InsuranceMapper,
): AbstractBackOfficeService<Insurance, InsuranceDto, String>(repository, mapper) {

}