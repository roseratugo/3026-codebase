package fr.mma.df.codinggame.api.feature.playerInsurance

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.insurance.InsuranceRepository
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class PlayerInsuranceService(
    private val playerRepository: PlayerRepository,
    private val insuranceRepository: InsuranceRepository,
    private val playerInsuranceRepository: PlayerInsuranceRepository,
    private val playerInsuranceMapper: PlayerInsuranceMapper
) {
    @Transactional
    fun subscribe(playerId: String, insuranceId: String): PlayerInsuranceDto {

        val player = playerRepository.findById(playerId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Player not found") }

        val insurance = insuranceRepository.findById(insuranceId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Insurance not found") }

        // 1) Vérifier qu'il n'a pas déjà souscrit
        val alreadySubscribed = playerInsuranceRepository
            .existsByPlayer_IdAndInsurance_Id(playerId, insuranceId)

        assertBusinessRule(
            condition = { !alreadySubscribed },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Player already subscribed to this insurance"
        )

        // 2) Vérifier qu'il a assez d'argent
        assertBusinessRule(
            condition = { player.money >= insurance.contribution },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Not enough money to subscribe to this insurance"
        )

        // 3) Retirer la contribution
        player.money -= insurance.contribution
        playerRepository.save(player)

        // 4) Créer la souscription
        val subscription = PlayerInsurance(
            player = player,
            insurance = insurance
        )

        val savedEntity = playerInsuranceRepository.save(subscription)
        return playerInsuranceMapper.toDto(savedEntity)
    }

    fun listForPlayer(playerId: String): List<PlayerInsurance> =
        playerInsuranceRepository.findAllByPlayer_Id(playerId)

    fun computeCoverage(playerInsurance: PlayerInsurance): Int {
        val insurance = playerInsurance.insurance
        val dynamicCoverage =
            insurance.coveredLevel -
                    (playerInsurance.riskCount * insurance.penaltyPerRisk)

        return dynamicCoverage.coerceIn(0, 100)
    }
}
