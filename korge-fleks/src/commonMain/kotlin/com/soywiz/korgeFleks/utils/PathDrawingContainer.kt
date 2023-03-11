package com.soywiz.korgeFleks.utils

import com.soywiz.korge.input.draggable
import com.soywiz.korge.input.mouse
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tween.moveTo
import com.soywiz.korim.color.ColorAdd
import com.soywiz.korim.color.Colors
import com.soywiz.korim.paint.Paint
import com.soywiz.korma.geom.Point
//import com.soywiz.korim.vector.StrokeInfo       //-- Korge 2.7.0
import com.soywiz.korma.geom.vector.StrokeInfo  //-- Korge 3.0.0
import com.soywiz.korma.geom.vector.lineTo
import com.soywiz.korma.geom.vector.moveTo

/**
 * TODO clean this up - remove if obsolete and not usable
 */
inline fun Container.showMotionPath(
    actualPosition: Point,
    callback: @ViewDslMarker PathDrawingContainer.() -> Unit = {}
) : PathDrawingContainer = PathDrawingContainer(actualPosition).addTo(this, callback).init()

class PathDrawingContainer(private val actualPosition: Point) : Container() {

    val g = graphics(renderer = GraphicsRenderer.SYSTEM)

    private val interpolatedPosition = circle(3.0, fill = Colors.YELLOW, stroke = Colors.DARKGRAY, strokeThickness = 1.0).centered
    private val positionIndicator = circle(3.0, fill = Colors.RED, stroke = Colors.DARKGRAY, strokeThickness = 1.0).centered

    private fun updatePathSections() {
        // TODO
        g.updateShape {
            clear()
            stroke(Colors.DIMGREY, info = StrokeInfo(thickness = 1.0)) {
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
            stroke(Colors.DARKGREEN, info = StrokeInfo(thickness = 1.0)) {
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
        circle(3.0, fill = color, stroke = Colors.DARKGRAY, strokeThickness = 1.0).centered
    }.position(point)
    return anchorView
}

fun Container.createPointController(point: Point, color: Paint, onMove: () -> Unit) {
    lateinit var circle: View
    lateinit var text: Text
    val textSize = 3.0
    val textSizeOnMouseOver = 15.0
    val anchorView = container {
        circle = circle(3.0, fill = color, stroke = Colors.DARKGRAY, strokeThickness = 1.0).centered
        text = text("", textSize).position(-5.0, 3.0)
    }.position(point)

    fun updateText() {
        text.text = "(${anchorView.x.toInt()}, ${anchorView.y.toInt()})"
    }
    circle.mouse {
        onOver {
            circle.colorAdd = ColorAdd(+256, 0, 0, 0)
            text.textSize = textSizeOnMouseOver
        }
        onOut {
            circle.colorAdd = ColorAdd(0, 0, 0, 0)
            text.textSize = textSize
        }
    }
    updateText()
    anchorView.draggable(circle) {
        point.x = anchorView.x
        point.y = anchorView.y
        updateText()
        onMove()
    }
}
