package korlibs.korge.fleks.assets.data.gameObject


data class CollisionData(
    // Anchor point of the collision rectangle to the pivot point of the entity
    val x: Int,
    val y: Int,
    // Size of the collision rectangle
    val width: Float,
    val height: Float
)
