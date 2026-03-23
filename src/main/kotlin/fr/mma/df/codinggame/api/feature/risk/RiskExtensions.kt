package fr.mma.df.codinggame.api.feature.risk

fun Risk.contains(x: Int, y: Int): Boolean {
    val minX = xOrigin - xRange
    val maxX = xOrigin + xRange
    val minY = yOrigin - yRange
    val maxY = yOrigin + yRange
    return x in minX..maxX && y in minY..maxY
}
