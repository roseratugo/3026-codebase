package fr.mma.df.codinggame.api.core.tools

import kotlin.math.sqrt

public final class Tools {
    companion object {
        fun distanceFromCenter(coordx: Int, coordy: Int): Double {
            val dx = coordx.toDouble()
            val dy = coordy.toDouble()

            val distanceFromCenter = sqrt(dx * dx + dy * dy)
            return distanceFromCenter
        }
    }
}