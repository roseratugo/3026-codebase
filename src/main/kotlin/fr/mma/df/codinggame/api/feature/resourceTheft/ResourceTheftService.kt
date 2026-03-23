package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.TheftStatus
import fr.mma.df.codinggame.api.core.enums.TheftTargetType
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.playerInsurance.PlayerInsuranceRepository
import fr.mma.df.codinggame.api.feature.playerInsurance.PlayerInsuranceService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.risk.RiskTypeEnum
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class ResourceTheftService(
    private val playerRepository: PlayerRepository,
    private val resourceTheftRepository: ResourceTheftRepository,
    private val theftConfigService: ResourceTheftConfigService,
    private val playerInsuranceRepository: PlayerInsuranceRepository,
    private val playerInsuranceService: PlayerInsuranceService,
    private val resourceBusinessService: ResourceBusinessService,
    private val playerService: PlayerService,
    private val resourceTheftMapper: ResourceTheftMapper,
    private val messageService: MessageService
) {

    /**
     * Applique le remboursement de l'assurance à la victime si elle en possède une
     * couvrant le risque RESOURCE_THEFT.
     * Le remboursement est proportionnel au taux de couverture de l'assurance.
     */
    private fun applyInsuranceRefund(
        victim: Player,
        resourceType: ResourceTypeEnum,
        amount: Long
    ) {
        val insurance = playerInsuranceRepository.findByPlayer_Id(victim.id!!) ?: return
        if (insurance.insurance.coveredRisk != RiskTypeEnum.RESOURCE_THEFT) return

        val coverage = playerInsuranceService.computeCoverage(insurance)
        val refund = ((amount * coverage) / 100).toInt()

        // Remboursement des ressources volées selon le taux de couverture
        resourceBusinessService.addQty(victim, resourceType, refund)
        playerRepository.save(victim)

        insurance.riskCount += 1
        playerInsuranceRepository.save(insurance)
    }

    /**
     * Déclenche un vol de ressources initié par un administrateur.
     * Permet de tester le système ou de créer des événements de jeu.
     * Le ratio de vol peut être forcé ou calculé automatiquement.
     */
    @Transactional
    open fun triggerAdminTheft(
        victimId: String,
        resourceType: ResourceTypeEnum,
        resolveDelayMinutes: Long,
        amountRatio: Double? = null
    ): ResourceTheft {

        val victim = playerRepository.findById(victimId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Victim not found") }

        // Utilise le ratio fourni ou calcule automatiquement le ratio de vol
        val ratio = amountRatio ?: theftConfigService.computeTheftRatio()

        assertBusinessRule(
            condition = { ratio > 0 },
            code = ExceptionCodeEnum.TECHNICAL_ERROR,
            message = "Theft ratio must be positive"
        )

        // Calcul du montant tenté en fonction du ratio et des ressources actuelles de la victime
        val amountAttempted = (resourceBusinessService.getQty(victim, resourceType) * ratio).toLong()

        return resourceTheftRepository.save(
            ResourceTheft(
                attacker = null,
                victim = victim,
                resourceType = resourceType,
                amountAttempted = amountAttempted,
                resolveAt = LocalDateTime.now().plusMinutes(resolveDelayMinutes),
                moneySpent = 0,
                successRate = 1.0,
                targetType = TheftTargetType.ADMIN_TRIGGERED
            )
        )
    }

    /**
     * Déclenche un vol de ressources initié par un joueur contre un autre.
     * Le joueur attaquant dépense de l'argent pour augmenter son taux de succès.
     * La victime est automatiquement le joueur ayant le plus de ressources du type ciblé.
     */
    @Transactional
    open fun triggerPlayerTheft(
        resourceType: ResourceTypeEnum,
        moneySpent: Long
    ): ResourceTheft {

        val attacker = playerService.getAuthenticatedPlayerEntity()

        // Un joueur ne peut avoir qu'un seul vol en attente à la fois
        val hasPending = resourceTheftRepository.existsByAttackerIdAndStatus(
            attacker.id!!,
            TheftStatus.PENDING
        )

        assertBusinessRule(
            condition = { !hasPending },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Player already has a pending theft"
        )

        assertBusinessRule(
            condition = { moneySpent >= 0 },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Money spent must be non-negative"
        )

        val successRate = theftConfigService.computeSuccessRate(moneySpent)

        // Déduction de l'argent dépensé par l'attaquant
        attacker.money = (attacker.money - moneySpent).coerceAtLeast(0f)
        playerRepository.save(attacker)

        // victim définie au resolve
        return resourceTheftRepository.save(
            ResourceTheft(
                attacker = attacker,
                victim = null,
                resourceType = resourceType,
                amountAttempted = 0,
                resolveAt = LocalDateTime.now().plusMinutes(theftConfigService.resolveDelayMinutes()),
                moneySpent = moneySpent,
                successRate = successRate,
                targetType = TheftTargetType.PLAYER_TRIGGERED
            )
        )
    }

    /**
     * Résout un vol en attente une fois le délai écoulé.
     * Si le vol réussit : les ressources sont retirées à la victime et ajoutées à l'attaquant.
     * Si le vol échoue : rien ne se passe.
     * Dans tous les cas, l'assurance de la victime est vérifiée pour un éventuel remboursement.
     */
    @Transactional
    open fun resolveTheft(theftId: String): ResourceTheft {

        // Récupération du vol à résoudre
        val theft = resourceTheftRepository.findById(theftId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Theft not found") }

        // Vérification que le vol n'est pas déjà résolu
        assertBusinessRule(
            condition = { theft.status == TheftStatus.PENDING },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Theft already resolved"
        )

        // Récupération du délai de résolution
        val resolveAt = theft.resolveAt.orThrowIfNull(
            code = ExceptionCodeEnum.TECHNICAL_ERROR,
            fieldName = "resolveAt"
        )

        // Vérification que le délai de résolution est bien écoulé
        assertBusinessRule(
            condition = { !resolveAt.isAfter(LocalDateTime.now()) },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Theft cannot be resolved yet"
        )

        if (theft.targetType == TheftTargetType.PLAYER_TRIGGERED) {
            // 0 = plus riche, 1 = deuxième, si victim == attacker on prend le deuxième plus riche
            val victims = playerRepository.findTop2ByResourcesTypeOrderByResourcesQuantityDesc(theft.resourceType)
                .orThrowIfNull(code = ExceptionCodeEnum.NOT_FOUND, fieldName = "victim")

            val victim = if(victims[0]?.id == theft.attacker?.id) victims[0] else {
                assertBusinessRule(
                    condition = { victims.size > 1 },
                    code = ExceptionCodeEnum.NOT_FOUND,
                    message = "Pas de victimes possible !"
                )
                victims[1]
            }

            val amountAttempted = (resourceBusinessService.getQty(victim!!, theft.resourceType) * theftConfigService.computeTheftRatio()).toLong()
            theft.victim = victim
            theft.amountAttempted = amountAttempted
        }

        val victim = theft.victim
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Victim not found")

        // Tirage au sort du succès selon le taux de succès calculé lors du déclenchement
        val success = Math.random() <= (theft.successRate ?: 0.0)

        if (success) {
            // Vol réussi — transfert des ressources de la victime vers l'attacker (si vol joueur)
            val stolenInt = theft.amountAttempted.toInt()

            // Retrait des ressources volées à la victime
            resourceBusinessService.payCost(victim, mapOf(theft.resourceType to stolenInt))
            playerRepository.save(victim)

            // Ajout des ressources volées à l'attaquant (si vol joueur, pas admin)
            theft.attacker?.let { attacker ->
                resourceBusinessService.addQty(attacker, theft.resourceType, stolenInt)
                playerRepository.save(attacker)
            }

            theft.amountStolen = theft.amountAttempted
            theft.status = TheftStatus.SUCCESS

            // Application du remboursement d'assurance si la victime en possède une
            applyInsuranceRefund(victim, theft.resourceType, theft.amountAttempted)
        } else {
            // Vol échoué — aucune ressource n'est transférée
            theft.amountStolen = 0
            theft.status = TheftStatus.FAILED
        }

        // Save in database
        val theftSaved = resourceTheftRepository.save(theft)

        // Publication des messages dans le broker pour prévenir la victime et le voleur
        messageService.publishTheftMessage(theft = theftSaved)

        return theftSaved
    }

    /**
     * Retourne la liste des vols en attente dont le délai de résolution est écoulé.
     * Utilisé par le scheduler pour résoudre automatiquement les vols.
     */
    open fun findPendingTheftsToResolve(now: LocalDateTime): List<ResourceTheft> =
        resourceTheftRepository.findAllByStatusAndResolveAtBefore(
            TheftStatus.PENDING,
            now
        )


    fun getPlayerThiefs(): List<ResourceTheftDto> {
        val player = playerService.getAuthenticatedPlayerEntity()
        val thiefs = resourceTheftRepository.findByAttackerId(player.id!!)

        return thiefs.map {
            resourceTheftMapper.toDto(it)
        }
    }
}