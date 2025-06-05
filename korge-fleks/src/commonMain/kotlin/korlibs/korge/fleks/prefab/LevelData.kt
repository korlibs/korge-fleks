package korlibs.korge.fleks.prefab

import korlibs.korge.fleks.assets.WorldData
import kotlin.native.concurrent.ThreadLocal


@ThreadLocal
object LevelPrefab {
    lateinit var levelData: WorldData
}