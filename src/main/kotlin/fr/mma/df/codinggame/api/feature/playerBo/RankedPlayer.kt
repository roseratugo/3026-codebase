package fr.mma.df.codinggame.api.feature.playerBo;

import fr.mma.df.codinggame.api.feature.resource.ResourceDto

data class RankedPlayer (
    val id: String,
    val name: String,
    val icon: String,
    val points: Int,
    val currentXPosition: Int,
    val currentYPosition: Int
)