package korlibs.korge.fleks.assets.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AssetConfig(
    val info: Info,
    val textures: List<String> = emptyList(),
    val images: Map<String, ImageInfo> = emptyMap()
) {
    @Serializable
    data class Info(
        @SerialName("v") val version: Int = 0,
        @SerialName("b") val build: Int = 0
    )

    @Serializable
    data class ImageInfo(
        @SerialName("w") val width: Int = 0,
        @SerialName("h") val height: Int = 0,
        val frames: List<ImageFrame> = emptyList()
    ) {
        @Serializable
        data class ImageFrame(
            val frame: Frame = Frame(),
            val x: Int = 0,
            val y: Int = 0,
            val duration: Int = 0
        ) {
            @Serializable
            data class Frame(
                @SerialName("i") val index: Int = 0,
                val x: Int = 0,
                val y: Int = 0,
                @SerialName("w") val width: Int = 0,
                @SerialName("h") val height: Int = 0
            )
        }
    }
}