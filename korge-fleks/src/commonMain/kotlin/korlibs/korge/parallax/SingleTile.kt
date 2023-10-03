package korlibs.korge.parallax

import korlibs.datastructure.IntArray2
import korlibs.image.bitmap.*
import korlibs.korge.view.Container
import korlibs.korge.view.SmoothedBmpSlice
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.addTo
import korlibs.korge.view.tiles.*
import korlibs.math.geom.Size

inline fun Container.repeatedImageView(
    bitmap: BmpSlice,
    repeatX: Boolean = false,
    repeatY: Boolean = false,
    smoothing: Boolean = true,
    block: @ViewDslMarker SingleTile.() -> Unit = {}
) = SingleTile(bitmap, smoothing).repeat(if (repeatX) TileMapRepeat.REPEAT else TileMapRepeat.NONE,
        if (repeatY) TileMapRepeat.REPEAT else TileMapRepeat.NONE
).addTo(this, block)

open class SingleTile(
    bitmap: BmpSlice,
    smoothing: Boolean = true
) : BaseTileMap(IntArray2(1, 1, 0), smoothing), SmoothedBmpSlice {
    override val tilesetTextures = Array<BitmapCoords?>(1) { bitmap }

    override var unscaledSize: Size = Size.ZERO

    override var bitmap: BitmapCoords = bitmap
        set(value) {
            if (field !== value) {
                field = value
                tilesetTextures[0] = value
                this.size = Size(value.width, value.height)
                tileWidth = width.toDouble()
                tileHeight = height.toDouble()
                tileSize = Size(width, height)
                contentVersion++
            }
        }
}
