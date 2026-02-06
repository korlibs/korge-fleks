package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.utils.EntityConfig
import kotlinx.serialization.Serializable


@Serializable
data class ChunkAssetInfo(
    val version: List<Int> = emptyList(),
    val entities: List<EntityConfig> = emptyList(),
    val levelMap: List<List<Int>> = emptyList()
) {
}