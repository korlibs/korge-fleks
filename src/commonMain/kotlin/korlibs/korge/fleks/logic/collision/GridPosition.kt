package korlibs.korge.fleks.logic.collision


data class GridPosition(
    var cx: Int = 0,  // Cell index
    var cy: Int = 0,
    var xr: Float = 0f,  // "Relative" position in the cell
    var yr: Float = 0f
) {
    // TODO remove var x,y if not needed
//    var x
//        get() = (cx + xr) * AppConfig.GRID_CELL_SIZE
//        set(value) {
//            cx = (value / AppConfig.GRID_CELL_SIZE).toInt()
//            xr = (value - cx * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE
//        }
//
//    var y
//        get() = (cy + yr) * AppConfig.GRID_CELL_SIZE
//        set(value) {
//            cy = (value / AppConfig.GRID_CELL_SIZE).toInt()
//            yr = (value - cy * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE
//
//        }

    fun applyOnX(x: Float, gridSize: Float) {
        xr += x / gridSize
        if (xr > 0) while (xr > 1) {
            xr -= 1f
            cx += 1
        }
        else if (xr < 0 ) while (xr < 0) {
            xr += 1f
            cx -= 1
        }
    }

    fun applyOnY(y: Float, gridSize: Float) {
        yr += y / gridSize
        if (yr > 0) while (yr > 1) {
            yr -= 1f
            cy += 1
        }
        else if (yr < 0 ) while (yr < 0) {
            yr += 1f
            cy -= 1
        }
    }

    fun setAndNormalizeX(ccx: Int, xxr: Float) {
        cx = ccx
        xr = xxr
        while (xr > 1f) {
            xr -= 1f
            cx += 1
        }
        while (xr < 0f) {
            xr += 1f
            cx -= 1
        }
    }

    fun setAndNormalizeY(ccy: Int, yyr: Float) {
        cy = ccy
        yr = yyr
        while (yr > 1f) {
            yr -= 1f
            cy += 1
        }
        while (yr < 0f) {
            yr += 1f
            cy -= 1
        }
    }
}
