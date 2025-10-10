package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.assets.data.ParallaxConfig.Mode


data class ParallaxBackgroundConfig(
    val mode: Mode,
    val width: Int,  // virtual size in the parallax effect (height for horizontal mode, width for vertical mode)
    val height: Int,
    val backgroundLayers: List<String> = emptyList(),
// TODO   val parallaxPlane: ParallaxConfigNew.ParallaxPlaneConfig = ParallaxPlaneConfig(),
    val foregroundLayers:  List<String> = emptyList()
) {

}
