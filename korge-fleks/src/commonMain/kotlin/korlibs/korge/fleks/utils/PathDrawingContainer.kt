package korlibs.korge.fleks.utils

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.paint.Paint
import korlibs.korge.input.draggable
import korlibs.korge.input.mouse
import korlibs.korge.view.*
import korlibs.math.geom.Point
import korlibs.math.geom.vector.StrokeInfo

/**
 * TODO clean this up - remove if obsolete and not usable
 */
inline fun Container.showMotionPath(
    actualPosition: Point,
    callback: @ViewDslMarker PathDrawingContainer.() -> Unit = {}
) : PathDrawingContainer = PathDrawingContainer(actualPosition).addTo(this, callback).init()

class PathDrawingContainer(private val actualPosition: Point) : Container() {

    val g = graphics(renderer = GraphicsRenderer.SYSTEM)

    private val interpolatedPosition = circle(3.0f, fill = Colors.YELLOW, stroke = Colors.DARKGRAY, strokeThickness = 1.0f).centered
    private val positionIndicator = circle(3.0f, fill = Colors.RED, stroke = Colors.DARKGRAY, strokeThickness = 1.0f).centered

    private fun updatePathSections() {
        // TODO
        g.updateShape {
            clear()
            stroke(Colors.DIMGREY, info = StrokeInfo(thickness = 1.0f)) {
//                pathObject.sections.forEachIndexed { index, _ ->
//                    if (index > 0) {
//                        moveTo(pathObject.sections[index - 1].xy)
//                        lineTo(pathObject.sections[index].xy)
//                    }
//                }
            }
        }
/*        graphics.clear()
        graphics.stroke(Colors.DIMGREY, info = StrokeInfo(thickness = 1.0)) {
            pathObject.sections.forEachIndexed { index, _ ->
                if (index > 0) {
                    moveTo(pathObject.sections[index - 1].xy)
                    lineTo(pathObject.sections[index].xy)
                }
            }
        }
// */
    }

    private fun updatePositionIndicator() {
        // TODO
//*
        // Draw line between actual position and next section point
        g.updateShape {
            stroke(Colors.DARKGREEN, info = StrokeInfo(thickness = 1.0f)) {
                moveTo(actualPosition)
//                lineTo(pathObject.getInterpolatedX(), pathObject.getInterpolatedY())
            }
        }
        // Update moving position indicator
//        interpolatedPosition.position(pathObject.getInterpolatedX(), pathObject.getInterpolatedY())
        positionIndicator.position(actualPosition)
// */
    }

    // Init must be called from outside this object - it's done in the inline function which creates this object - awesome how this works :)
    fun init() : PathDrawingContainer {
//        pathObject.sections.forEach { section ->
//            createPointController(section.xy, Colors.GREEN) { updatePathSections() }
//        }
        return this
    }

    init {
        addUpdater {
            updatePathSections()
//            updatePositionIndicator(pathObject.getActiveSection())
        }
    }
}

fun Container.showPoint(point: Point, color: Paint) : Container {
    val anchorView = container {
        circle(3.0f, fill = color, stroke = Colors.DARKGRAY, strokeThickness = 1.0f).centered
    }.position(point)
    return anchorView
}

fun Container.createPointController(point: Point, color: Paint, onMove: () -> Unit) {
    lateinit var circle: View
    lateinit var text: Text
    val textSize = 3.0f
    val textSizeOnMouseOver = 15.0f
    val anchorView = container {
        circle = circle(3.0f, fill = color, stroke = Colors.DARKGRAY, strokeThickness = 1.0f).centered
        text = text("", textSize).position(-5.0, 3.0)
    }.position(point)

    fun updateText() {
        text.text = "(${anchorView.x.toInt()}, ${anchorView.y.toInt()})"
    }
    circle.mouse {
        onOver {
            circle.colorMul(RGBA(+256, 0, 0, 0))
            text.textSize = textSizeOnMouseOver.toDouble()
        }
        onOut {
            circle.colorMul(RGBA(0, 0, 0, 0))
            text.textSize = textSize.toDouble()
        }
    }
    updateText()
    anchorView.draggable(circle) {
//        point.x = anchorView.x
//        point.y = anchorView.y
        updateText()
        onMove()
    }
}
