package fr.mma.df.codinggame.api.feature.map

class MapConfiguration(

    val width: Int = MapDefaults.WIDTH,
    val height: Int = MapDefaults.HEIGHT,
    val seed: Long = MapDefaults.SEED,
    val scaleFactor: Double = MapDefaults.SCALE_FACTOR,
    val saving: Boolean = MapDefaults.SAVING,
    val minIslandSize: Int = MapDefaults.MIN_ISLAND_SIZE,
    val maxIslandSize: Int = MapDefaults.MAX_ISLAND_SIZE,

    //distance en cases du centre
    val zones: List<Int> = listOf(10, 50, 150, 300),

    //élévation du terrain qui determine le type de casses,
    // respectivement SEA, SAND, ROCKS, toute valeurs au dessus de la 3 eme entré serra MOUNTAIN
    val types: List<Double> = listOf(0.55, 0.65, 0.90)
) {
    // Liste des directions possibles : droite, bas, gauche, haut
    val directions: List<Pair<Int, Int>> = listOf(
        0 to 1,   // droite
        1 to 0,   // bas
        0 to -1,  // gauche
        -1 to 0   // haut
    )
}
