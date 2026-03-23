package fr.mma.df.codinggame.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConditionalOnProperty(
    name = ["scheduling.enabled"],
    havingValue = "true",
    matchIfMissing = true
) // permet de déactiver le scheduling pour les test
@EnableScheduling
@EnableAspectJAutoProxy
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}