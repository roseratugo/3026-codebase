package fr.mma.df.codinggame.api.feature.risk

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.enums.TaxStateEnum
import fr.mma.df.codinggame.api.core.enums.TaxTypeEnum
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.insurance.Insurance
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import fr.mma.df.codinggame.api.feature.playerInsurance.PlayerInsuranceRepository
import fr.mma.df.codinggame.api.feature.playerInsurance.PlayerInsuranceService
import fr.mma.df.codinggame.api.feature.ship.Ship
import fr.mma.df.codinggame.api.feature.ship.ShipRepository
import fr.mma.df.codinggame.api.feature.shiprisk.ShipRisk
import fr.mma.df.codinggame.api.feature.shiprisk.ShipRiskRepository
import fr.mma.df.codinggame.api.feature.tax.TaxDto
import fr.mma.df.codinggame.api.feature.tax.TaxService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RiskService(
    private val riskRepository: RiskRepository,
    private val shipRepository: ShipRepository,
    private val mapper: RiskMapper,
    private val shipRiskRepository: ShipRiskRepository,
    private val playerInsuranceRepository: PlayerInsuranceRepository,
    private val playerInsuranceService: PlayerInsuranceService,
    private val taxService: TaxService,
    private val messageService: MessageService
) : AbstractBackOfficeService<Risk, RiskDto, String>(riskRepository, mapper) {

    override fun create(dto: RiskDto): RiskDto {
        val risk = validateAndSaveRisk(dto)
        val dtoOut = mapper.toDto(risk)
        messageService.publishMessage(MessageType.RISQUE_APPARU, dtoOut)
        return dtoOut
    }

    fun findRisksAffecting(x: Int, y: Int): List<Risk> =
        riskRepository.findAll().filter { it.contains(x, y) }

    private fun validateAndSaveRisk(dto: RiskDto): Risk {
        val entity = mapper.toEntity(dto)
        entity.type.orThrowIfNull(ExceptionCodeEnum.TECHNICAL_ERROR, "riskType")

        assertBusinessRule(
            { entity.xRange >= 0 && entity.yRange >= 0 },
            ExceptionCodeEnum.TECHNICAL_ERROR,
            "Risk ranges must be non-negative."
        )

        assertBusinessRule(
            { entity.severity in 1..10 },
            ExceptionCodeEnum.TECHNICAL_ERROR,
            "Severity must be between 1 and 10."
        )

        return riskRepository.save(entity)
    }

    fun applyRiskToShip(ship: Ship, risk: Risk) {
        applyInsuranceAndTax(ship, risk)
        immobilizeShip(ship, risk)
        saveShipRisk(ship, risk)
    }

    private fun immobilizeShip(ship: Ship, risk: Risk) {
        ship.immobilized = true
        ship.distressCause = risk.type.name
        ship.availableMove = 0
        shipRepository.save(ship)
    }

    private fun saveShipRisk(ship: Ship, risk: Risk) {
        val shipRisk = ShipRisk(
            ship = ship,
            risk = risk,
            immobilizedAt = LocalDateTime.now()
        )
        shipRiskRepository.save(shipRisk)
    }

    private fun applyInsuranceAndTax(ship: Ship, risk: Risk) {
        val player = ship.player ?: return

        val playerInsurance = playerInsuranceRepository.findByPlayer_Id(player.id!!)
        val insurance = playerInsurance?.insurance

        val isCovered = insurance?.coveredRisk == risk.type

        val amount = when {
            insurance == null -> computeFullTax(risk)
            isCovered -> computeCoveredTax(risk, insurance, playerInsurance.riskCount)
            else -> computeFullTax(risk)
        }

        createTax(player, amount)

        if (isCovered) {
            playerInsurance!!.apply {
                riskCount += 1
                playerInsuranceRepository.save(this)
            }
        }
    }


    private fun computeFullTax(risk: Risk): Int =
        100 * risk.severity

    private fun computeCoveredTax(risk: Risk, insurance: Insurance, riskCount: Int): Int {
        val baseTax = computeFullTax(risk)

        val coveragePercent = insurance.coveredLevel.coerceIn(0, 100)
        val coveredAmount = (baseTax * coveragePercent / 100.0).toInt()

        val penalty = insurance.penaltyPerRisk * riskCount

        return (baseTax - coveredAmount + penalty)
            .coerceAtLeast(0)
    }


    private fun createTax(player: Player, amount: Int) {
        val taxDto = TaxDto(
            type = TaxTypeEnum.RESCUE,
            state = TaxStateEnum.DUE,
            amount = amount,
            player = PlayerLiteDto(player.id!!, player.name, player.color)
        )
        taxService.create(taxDto)
    }
}
