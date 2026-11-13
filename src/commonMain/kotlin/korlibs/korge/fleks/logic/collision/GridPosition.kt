package korlibs.korge.fleks.logic.collision


data class GridPosition(
    var cx: Int = 0,  // Cell index
    var cy: Int = 0,
    var xr: Double = 0.0,  // "Relative" position in the cell
    var yr: Double = 0.0
) {

    fun applyOnX(x: Double, gridSize: Double) {
        xr += x / gridSize
        if (xr > 0) while (xr > 1) {
            xr -= 1.0
            cx += 1
        }
        else if (xr < 0 ) while (xr < 0) {
            xr += 1.0
            cx -= 1
        }
    }

    fun applyOnY(y: Double, gridSize: Double) {
        yr += y / gridSize
        if (yr > 0) while (yr > 1) {
            yr -= 1.0
            cy += 1
        }
        else if (yr < 0 ) while (yr < 0) {
            yr += 1.0
            cy -= 1
        }
    }

    fun setAndNormalizeX(ccx: Int, xxr: Double) {
        cx = ccx
        xr = xxr
        while (xr > 1.0) {
            xr -= 1.0
            cx += 1
        }
        while (xr < 0.0) {
            xr += 1.0
            cx -= 1
        }
    }

    fun setAndNormalizeY(ccy: Int, yyr: Double) {
        cy = ccy
        yr = yyr
        while (yr > 1.0) {
            yr -= 1.0
            cy += 1
        }
        while (yr < 0.0) {
            yr += 1.0
            cy -= 1
        }
    }
}
