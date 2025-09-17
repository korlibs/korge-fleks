package korlibs.korge.fleks.assets.data.gameObject


data class StateConfig(
    val entities: MutableMap<String, EntityStateConfig>,
    val collisionBox: CollisionData? = null,
)
