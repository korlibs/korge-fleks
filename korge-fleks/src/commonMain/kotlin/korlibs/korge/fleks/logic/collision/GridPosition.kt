package korlibs.korge.fleks.logic.collision

import korlibs.korge.fleks.utils.AppConfig


data class GridPosition(
    var cx: Int = 0,  // Cell index
    var cy: Int = 0,
    var xr: Float = 0f,  // "Relative" position in the cell
    var yr: Float = 0f
) {
    var x
        get() = (cx + xr) * AppConfig.GRID_CELL_SIZE
        set(value) {
            cx = (value / AppConfig.GRID_CELL_SIZE).toInt()
            xr = (value - cx * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE
        }

    var y
        get() = (cy + yr) * AppConfig.GRID_CELL_SIZE
        set(value) {
            cy = (value / AppConfig.GRID_CELL_SIZE).toInt()
            yr = (value - cy * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE

        }

    fun applyOnX(x : Int) {applyOnX(x.toFloat()) }
    fun applyOnY(y : Int) {applyOnY(y.toFloat()) }

    fun applyOnX(x: Float) {
        xr += x / AppConfig.GRID_CELL_SIZE
        if (xr > 0) while (xr > 1) {
            xr--
            cx++
        }
        else if (xr < 0 ) while (xr < 0) {
            xr++
            cx--
        }
    }

    fun applyOnY(y: Float) {
        yr += y / AppConfig.GRID_CELL_SIZE
        if (yr > 0) while (yr > 1) {
            yr--
            cy++
        }
        else if (yr < 0 ) while (yr < 0) {
            yr++
            cy--
        }
    }
}
