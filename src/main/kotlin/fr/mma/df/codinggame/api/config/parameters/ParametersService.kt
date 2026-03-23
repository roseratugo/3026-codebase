package fr.mma.df.codinggame.api.config.parameters

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevel
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelRepository
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevel
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevelRepository
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class ParametersService(
    private val shipLevelRepository: ShipLevelRepository,
    private val storageLevelRepository: StorageLevelRepository,
    private val parametersRepository: ParametersRepository,
    private val serverParameters: ServerParameters
) {
    private val logger = LoggerFactory.getLogger("ParametersService")

    //@PostConstruct
    @Transactional
    open fun init() {
        val parsed = parseIni()

        // Sync ShipLevels en BDD depuis le .ini
        parsed.shipLevels.forEach { syncShipLevel(it) }

        // Sync StorageLevels en BDD depuis le .ini
        parsed.storageLevels.forEach { syncStorageLevel(it) }

        // Sync des paramètres gameplay
        val existingParams = parametersRepository.findById(1L)
        if (existingParams.isPresent) {
            val params = existingParams.get()
            params.scheduledResourceIntervalMs = parsed.scheduledResourceIntervalMs
            params.defaultResourceQuotient = parsed.defaultResourceQuotient
            params.defaultMoney = parsed.defaultMoney
            params.theftMaxSuccessRate = parsed.theftMaxSuccessRate
            params.theftResolveDelayMinutes = parsed.theftResolveDelayMinutes
            params.islandReward1 = parsed.islandReward1
            params.islandReward2 = parsed.islandReward2
            params.islandReward3 = parsed.islandReward3
            params.islandRewardDefault = parsed.islandRewardDefault
            parametersRepository.save(params)
        } else {
            parametersRepository.save(Parameters(
                scheduledResourceIntervalMs = parsed.scheduledResourceIntervalMs,
                defaultResourceQuotient = parsed.defaultResourceQuotient,
                defaultMoney = parsed.defaultMoney,
                theftMaxSuccessRate = parsed.theftMaxSuccessRate,
                theftResolveDelayMinutes = parsed.theftResolveDelayMinutes,
                islandReward1 = parsed.islandReward1,
                islandReward2 = parsed.islandReward2,
                islandReward3 = parsed.islandReward3,
                islandRewardDefault = parsed.islandRewardDefault
            ))
        }

        // Chargement en mémoire dans ServerParameters
        serverParameters.shipLevels = shipLevelRepository.findAll().map { it.toShipParameterDto() }
        serverParameters.storageLevels = storageLevelRepository.findAll().map { it.toStorageParameterDto() }
        serverParameters.scheduledResourceIntervalMs = parsed.scheduledResourceIntervalMs
        serverParameters.defaultResourceQuotient = parsed.defaultResourceQuotient
        serverParameters.defaultMoney = parsed.defaultMoney
        serverParameters.theftMaxSuccessRate = parsed.theftMaxSuccessRate
        serverParameters.theftResolveDelayMinutes = parsed.theftResolveDelayMinutes
        serverParameters.islandReward1 = parsed.islandReward1
        serverParameters.islandReward2 = parsed.islandReward2
        serverParameters.islandReward3 = parsed.islandReward3
        serverParameters.islandRewardDefault = parsed.islandRewardDefault

        logger.info("ServerParameters initialisé : ${serverParameters.shipLevels.size} ship levels, ${serverParameters.storageLevels.size} storage levels")
    }

    fun getParameters(): ParametersDto {
        return ParametersDto(
            shipLevels = serverParameters.shipLevels,
            storageLevels = serverParameters.storageLevels,
            gameplay = GameplayParametersDto(
                scheduledResourceIntervalMs = serverParameters.scheduledResourceIntervalMs,
                defaultResourceQuotient = serverParameters.defaultResourceQuotient,
                defaultMoney = serverParameters.defaultMoney,
                theftMaxSuccessRate = serverParameters.theftMaxSuccessRate,
                theftResolveDelayMinutes = serverParameters.theftResolveDelayMinutes,
                islandReward1 = serverParameters.islandReward1,
                islandReward2 = serverParameters.islandReward2,
                islandReward3 = serverParameters.islandReward3,
                islandRewardDefault = serverParameters.islandRewardDefault
            )
        )
    }

    @Transactional
    open fun updateGameplay(dto: GameplayParametersDto): ParametersDto {
        val params = parametersRepository.findById(1L)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Parameters introuvable") }
        params.scheduledResourceIntervalMs = dto.scheduledResourceIntervalMs
        params.defaultResourceQuotient = dto.defaultResourceQuotient
        params.defaultMoney = dto.defaultMoney
        params.theftMaxSuccessRate = dto.theftMaxSuccessRate
        params.theftResolveDelayMinutes = dto.theftResolveDelayMinutes
        params.islandReward1 = dto.islandReward1
        params.islandReward2 = dto.islandReward2
        params.islandReward3 = dto.islandReward3
        params.islandRewardDefault = dto.islandRewardDefault

        serverParameters.scheduledResourceIntervalMs = dto.scheduledResourceIntervalMs
        serverParameters.defaultResourceQuotient = dto.defaultResourceQuotient
        serverParameters.defaultMoney = dto.defaultMoney
        serverParameters.theftMaxSuccessRate = dto.theftMaxSuccessRate
        serverParameters.theftResolveDelayMinutes = dto.theftResolveDelayMinutes
        serverParameters.islandReward1 = dto.islandReward1
        serverParameters.islandReward2 = dto.islandReward2
        serverParameters.islandReward3 = dto.islandReward3
        serverParameters.islandRewardDefault = dto.islandRewardDefault
        parametersRepository.save(params)

        return getParameters()
    }

    @Transactional
    open fun updateShipLevels(shipLevels: List<ShipLevelParameterDto>): ParametersDto {
        shipLevels.forEach { param ->
            val entity = shipLevelRepository.findById(param.id)
                .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "ShipLevel ${param.id} introuvable") }
            entity.name = param.name
            entity.visibilityRange = param.visibilityRange
            entity.maxMovement = param.maxMovement
            entity.speed = param.speed
            entity.costResourcePers = param.costResourcePers
            entity.costResourceA = param.costResourceA
            entity.costResourceB = param.costResourceB
            shipLevelRepository.save(entity)
        }

        serverParameters.shipLevels = shipLevelRepository.findAll().map { it.toShipParameterDto() }

        return getParameters()
    }

    @Transactional
    open fun updateStorageLevels(storageLevels: List<StorageLevelParameterDto>): ParametersDto {
        storageLevels.forEach { param ->
            val entity = storageLevelRepository.findById(param.id)
                .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "StorageLevel ${param.id} introuvable") }
            entity.name = param.name
            entity.maxResourcePers = param.maxResourcePers
            entity.maxResourceA = param.maxResourceA
            entity.maxResourceB = param.maxResourceB
            entity.costResourcePers = param.costResourcePers
            entity.costResourceA = param.costResourceA
            entity.costResourceB = param.costResourceB
            storageLevelRepository.save(entity)
        }

        serverParameters.storageLevels = storageLevelRepository.findAll().map { it.toStorageParameterDto() }

        return getParameters()
    }

    /**
     * Synchronise un ShipLevel parsé depuis le .ini avec la BDD.
     * Insère si absent, met à jour si différent.
     */
    private fun syncShipLevel(parsed: ShipLevelParameterDto) {
        val existing = shipLevelRepository.findById(parsed.id)
        if (existing.isPresent) {
            val entity = existing.get()
            if (isDifferentShip(entity, parsed)) {
                logger.info("Mise à jour du ShipLevel ${parsed.id}")
                entity.name = parsed.name
                entity.visibilityRange = parsed.visibilityRange
                entity.maxMovement = parsed.maxMovement
                entity.speed = parsed.speed
                entity.costResourcePers = parsed.costResourcePers
                entity.costResourceA = parsed.costResourceA
                entity.costResourceB = parsed.costResourceB
                shipLevelRepository.save(entity)
            }
        } else {
            logger.info("Insertion du ShipLevel ${parsed.id}")
            shipLevelRepository.save(ShipLevel(
                name = parsed.name,
                visibilityRange = parsed.visibilityRange,
                maxMovement = parsed.maxMovement,
                speed = parsed.speed,
                costResourcePers = parsed.costResourcePers,
                costResourceA = parsed.costResourceA,
                costResourceB = parsed.costResourceB
            ))
        }
    }

    /**
     * Synchronise un StorageLevel parsé depuis le .ini avec la BDD.
     * Insère si absent, met à jour si différent.
     */
    private fun syncStorageLevel(parsed: StorageLevelParameterDto) {
        val existing = storageLevelRepository.findById(parsed.id)
        if (existing.isPresent) {
            val entity = existing.get()
            if (isDifferentStorage(entity, parsed)) {
                logger.info("Mise à jour du StorageLevel ${parsed.id}")
                entity.name = parsed.name
                entity.maxResourcePers = parsed.maxResourcePers
                entity.maxResourceA = parsed.maxResourceA
                entity.maxResourceB = parsed.maxResourceB
                entity.costResourcePers = parsed.costResourcePers
                entity.costResourceA = parsed.costResourceA
                entity.costResourceB = parsed.costResourceB
                storageLevelRepository.save(entity)
            }
        } else {
            logger.info("Insertion du StorageLevel ${parsed.id}")
            storageLevelRepository.save(StorageLevel(
                id = parsed.id,
                name = parsed.name,
                maxResourcePers = parsed.maxResourcePers,
                maxResourceA = parsed.maxResourceA,
                maxResourceB = parsed.maxResourceB,
                costResourcePers = parsed.costResourcePers,
                costResourceA = parsed.costResourceA,
                costResourceB = parsed.costResourceB
            ))
        }
    }

    private data class ParsedIni(
        val shipLevels: List<ShipLevelParameterDto>,
        val storageLevels: List<StorageLevelParameterDto>,
        val scheduledResourceIntervalMs: Long,
        val defaultResourceQuotient: Int,
        val defaultMoney: Double,
        val theftMaxSuccessRate: Double,
        val theftResolveDelayMinutes: Long,
        val islandReward1: Int,
        val islandReward2: Int,
        val islandReward3: Int,
        val islandRewardDefault: Int
    )

    private fun parseIni(): ParsedIni {
        val lines = this::class.java.getResourceAsStream("/parameters.ini")
            ?.bufferedReader()?.readLines()
            ?: return ParsedIni(emptyList(), emptyList(), 600000L, 200, 200.0, 0.5, 20L, 100, 75, 50, 25)

        val shipLevels = mutableListOf<ShipLevelParameterDto>()
        val storageLevels = mutableListOf<StorageLevelParameterDto>()

        var currentSection = ""
        var currentId: Int? = null

        // Ship fields
        var shipName = ""
        var visibilityRange = 0
        var maxMovement = 0
        var speed = 0L
        var shipCostResourcePers = 0
        var shipCostResourceA = 0
        var shipCostResourceB = 0

        // Storage fields
        var storageName = ""
        var maxResourcePers = 0
        var maxResourceA = 0
        var maxResourceB = 0
        var costResourcePers = 0
        var costResourceA = 0
        var costResourceB = 0

        // Gameplay fields
        var scheduledResourceIntervalMs = 300000L
        var defaultResourceQuotient = 300
        var defaultMoney = 200.0
        var theftMaxSuccessRate = 0.5
        var theftResolveDelayMinutes = 20L
        var islandReward1 = 100
        var islandReward2 = 75
        var islandReward3 = 50
        var islandRewardDefault = 25

        fun flushShip() {
            if (currentSection == "ship" && currentId != null) {
                shipLevels.add(ShipLevelParameterDto(
                    id = currentId!!,
                    name = shipName,
                    visibilityRange = visibilityRange,
                    maxMovement = maxMovement,
                    speed = speed,
                    costResourcePers = shipCostResourcePers,
                    costResourceA = shipCostResourceA,
                    costResourceB = shipCostResourceB
                ))
                // Reset des champs ship pour le prochain bloc
                shipCostResourcePers = 0
                shipCostResourceA = 0
                shipCostResourceB = 0
            }
        }

        fun flushStorage() {
            if (currentSection == "storage" && currentId != null) {
                storageLevels.add(StorageLevelParameterDto(
                    id = currentId!!,
                    name = storageName,
                    maxResourcePers = maxResourcePers,
                    maxResourceA = maxResourceA,
                    maxResourceB = maxResourceB,
                    costResourcePers = costResourcePers,
                    costResourceA = costResourceA,
                    costResourceB = costResourceB
                ))
            }
        }

        fun flush() {
            flushShip()
            flushStorage()
        }

        lines.forEach { line ->
            when {
                line.startsWith("[ship.") -> {
                    flush()
                    currentSection = "ship"
                    currentId = line.removePrefix("[ship.").removeSuffix("]").toInt()
                }
                line.startsWith("[storage.") -> {
                    flush()
                    currentSection = "storage"
                    currentId = line.removePrefix("[storage.").removeSuffix("]").toInt()
                }
                line.startsWith("[gameplay]") -> {
                    flush()
                    currentSection = "gameplay"
                    currentId = null
                }
                // Ship fields
                currentSection == "ship" && line.startsWith("name=") -> shipName = line.removePrefix("name=")
                currentSection == "ship" && line.startsWith("visibilityRange=") -> visibilityRange = line.removePrefix("visibilityRange=").toInt()
                currentSection == "ship" && line.startsWith("maxMovement=") -> maxMovement = line.removePrefix("maxMovement=").toInt()
                currentSection == "ship" && line.startsWith("speed=") -> speed = line.removePrefix("speed=").toLong()
                currentSection == "ship" && line.startsWith("costResourcePers=") -> shipCostResourcePers = line.removePrefix("costResourcePers=").toInt()
                currentSection == "ship" && line.startsWith("costResourceA=") -> shipCostResourceA = line.removePrefix("costResourceA=").toInt()
                currentSection == "ship" && line.startsWith("costResourceB=") -> shipCostResourceB = line.removePrefix("costResourceB=").toInt()
                // Storage fields
                currentSection == "storage" && line.startsWith("name=") -> storageName = line.removePrefix("name=")
                currentSection == "storage" && line.startsWith("maxResourcePers=") -> maxResourcePers = line.removePrefix("maxResourcePers=").toInt()
                currentSection == "storage" && line.startsWith("maxResourceA=") -> maxResourceA = line.removePrefix("maxResourceA=").toInt()
                currentSection == "storage" && line.startsWith("maxResourceB=") -> maxResourceB = line.removePrefix("maxResourceB=").toInt()
                currentSection == "storage" && line.startsWith("costResourcePers=") -> costResourcePers = line.removePrefix("costResourcePers=").toInt()
                currentSection == "storage" && line.startsWith("costResourceA=") -> costResourceA = line.removePrefix("costResourceA=").toInt()
                currentSection == "storage" && line.startsWith("costResourceB=") -> costResourceB = line.removePrefix("costResourceB=").toInt()
                // Gameplay fields
                currentSection == "gameplay" && line.startsWith("scheduledResourceIntervalMs=") ->
                    scheduledResourceIntervalMs = line.removePrefix("scheduledResourceIntervalMs=").toLong()
                currentSection == "gameplay" && line.startsWith("defaultResourceQuotient=") ->
                    defaultResourceQuotient = line.removePrefix("defaultResourceQuotient=").toInt()
                currentSection == "gameplay" && line.startsWith("defaultMoney=") ->
                    defaultMoney = line.removePrefix("defaultMoney=").toDouble()
                currentSection == "gameplay" && line.startsWith("theftMaxSuccessRate=") ->
                    theftMaxSuccessRate = line.removePrefix("theftMaxSuccessRate=").toDouble()
                currentSection == "gameplay" && line.startsWith("theftResolveDelayMinutes=") ->
                    theftResolveDelayMinutes = line.removePrefix("theftResolveDelayMinutes=").toLong()
                currentSection == "gameplay" && line.startsWith("islandReward1=") ->
                    islandReward1 = line.removePrefix("islandReward1=").toInt()
                currentSection == "gameplay" && line.startsWith("islandReward2=") ->
                    islandReward2 = line.removePrefix("islandReward2=").toInt()
                currentSection == "gameplay" && line.startsWith("islandReward3=") ->
                    islandReward3 = line.removePrefix("islandReward3=").toInt()
                currentSection == "gameplay" && line.startsWith("islandRewardDefault=") ->
                    islandRewardDefault = line.removePrefix("islandRewardDefault=").toInt()
            }
        }
        flush() // dernier bloc

        return ParsedIni(shipLevels, storageLevels, scheduledResourceIntervalMs, defaultResourceQuotient, defaultMoney, theftMaxSuccessRate, theftResolveDelayMinutes, islandReward1, islandReward2, islandReward3, islandRewardDefault)
    }

    /**
     * Recharge les données depuis la base de données.
     */
    fun reloadFromDatabase() {

        // Recharge depuis la bdd toutes les valeurs des ShipLevels
        serverParameters.shipLevels = shipLevelRepository.findAll().map { it.toShipParameterDto() }

        // Recharge depuis la bdd toutes les valeurs des StorageLevels
        serverParameters.storageLevels = storageLevelRepository.findAll().map { it.toStorageParameterDto() }

        // Recharge les paramètres theft et island depuis la BDD
        val params = parametersRepository.findById(1L).orElse(null)
        if (params != null) {
            serverParameters.defaultResourceQuotient = params.defaultResourceQuotient
            serverParameters.defaultMoney = params.defaultMoney
            serverParameters.theftMaxSuccessRate = params.theftMaxSuccessRate
            serverParameters.theftResolveDelayMinutes = params.theftResolveDelayMinutes
            serverParameters.islandReward1 = params.islandReward1
            serverParameters.islandReward2 = params.islandReward2
            serverParameters.islandReward3 = params.islandReward3
            serverParameters.islandRewardDefault = params.islandRewardDefault
        }
    }


    /**
     * Compare un ShipLevel BDD avec un ShipLevelParameterDto parsé depuis le .ini.
     * Retourne true si une différence est détectée.
     */
    private fun isDifferentShip(entity: ShipLevel, dto: ShipLevelParameterDto): Boolean {
        return entity.name != dto.name
                || entity.visibilityRange != dto.visibilityRange
                || entity.maxMovement != dto.maxMovement
                || entity.speed != dto.speed
                || entity.costResourcePers != dto.costResourcePers
                || entity.costResourceA != dto.costResourceA
                || entity.costResourceB != dto.costResourceB
    }

    /**
     * Compare un StorageLevel BDD avec un StorageLevelParameterDto parsé depuis le .ini.
     * Retourne true si une différence est détectée.
     */
    private fun isDifferentStorage(entity: StorageLevel, dto: StorageLevelParameterDto): Boolean {
        return entity.name != dto.name
                || entity.maxResourcePers != dto.maxResourcePers
                || entity.maxResourceA != dto.maxResourceA
                || entity.maxResourceB != dto.maxResourceB
                || entity.costResourcePers != dto.costResourcePers
                || entity.costResourceA != dto.costResourceA
                || entity.costResourceB != dto.costResourceB
    }
}

/**
 * Extension : convertit un ShipLevel JPA en ShipLevelParameterDto pour ServerParameters
 */
fun ShipLevel.toShipParameterDto() = ShipLevelParameterDto(
    id = requireNotNull(id),
    name = name,
    visibilityRange = visibilityRange,
    maxMovement = maxMovement,
    speed = speed,
    costResourcePers = costResourcePers ?: 0,
    costResourceA = costResourceA ?: 0,
    costResourceB = costResourceB ?: 0
)

/**
 * Extension : convertit un StorageLevel JPA en StorageLevelParameterDto pour ServerParameters
 */
fun StorageLevel.toStorageParameterDto() = StorageLevelParameterDto(
    id = id,
    name = name,
    maxResourcePers = maxResourcePers,
    maxResourceA = maxResourceA,
    maxResourceB = maxResourceB,
    costResourcePers = costResourcePers,
    costResourceA = costResourceA,
    costResourceB = costResourceB
)