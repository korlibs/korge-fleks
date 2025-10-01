package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.assets.data.ParallaxConfigNew.Mode


data class ParallaxBackgroundConfig(
    val offset: Int,
    val mode: Mode,
    val size: Float,  // virtual size in the parallax effect (height for horizontal mode, width for vertical mode)
    val backgroundLayers: List<String> = emptyList(),
// TODO   val parallaxPlane: ParallaxConfigNew.ParallaxPlaneConfig = ParallaxPlaneConfig(),
    val foregroundLayers:  List<String> = emptyList()
) {

}
