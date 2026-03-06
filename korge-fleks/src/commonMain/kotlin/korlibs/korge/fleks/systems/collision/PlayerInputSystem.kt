package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.event.Key
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.input.keys
import korlibs.korge.view.Container
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.addTo
import korlibs.korge.view.image
import korlibs.math.clamp
import korlibs.math.interpolation.EASE_IN_QUAD
import korlibs.math.interpolation.EASE_OUT_QUAD
import korlibs.math.interpolation.Easing
import korlibs.math.roundDecimalPlaces
import kotlin.time.Duration.Companion.seconds


//inline fun Container.virtualGamePad(
//    assetStore: AssetStore,
//    leftKnobOffsetX: Int = 0,
//    leftKnobOffsetY: Int = 0,
//    knobScale: Float = 1f,
//    block: @ViewDslMarker VirtualGamePad.() -> Unit = {}
//) = VirtualGamePad(assetStore, leftKnobOffsetX, leftKnobOffsetY, knobScale).addTo(this, block)

interface InputSystem {
    var up: Boolean
    // TODO
}

class VirtualGamePad(
    assetStore: AssetStore,
    leftKnobOffsetX: Int,
    leftKnobOffsetY: Int,
    private val knobScale: Float,
) : Container(), InputSystem {
    // directions of digital joystick
    override var up: Boolean = false
    var down = false
    var right = false
    var left = false
    var justUp = false
    var justDown = false
    var justRight = false
    var justLeft = false
    var lx = 0f
    var ly = 0f
    var rx = 0f
    var ry = 0f
    var attack = false
    var justReleasedAttack = false
    var attackDirection = 0.0f
    var attackIndex = 0

    private val alphaInDuration = 0.4.seconds
    private val alphaOutDuration = 0.4.seconds
    private val alphaInEasing = Easing.EASE_OUT_QUAD
    private val alphaOutEasing = Easing.EASE_IN_QUAD

    private var pressed = true  // will be reset to false after fadeIn finished

    data class KnobData(
        var fingerId: Int = -1,
        var active: Boolean = false,
        var startX: Double = 0.0,  // TODO not used yet, but can be used to calculate movement delta from initial touch position instead of joystick pivot point
        var startY: Double = 0.0   //      maybe we make this configurable from the options menu
    )

    // Finger data for both knobs (multitouch)
    private val leftKnob = KnobData()
    private var rightKnob = KnobData()

    fun onTouchStart() {
        // Invalidate/untouch all knobs to be able to detect new touches in onTouchMove
        leftKnob.active = false
        rightKnob.active = false
        // Reset just triggers
        justUp = false
        justDown = false
        justRight = false
        justLeft = false
    }
    fun onTouchEnd() {
        // Find fingers which ended movement and reset knob positions
        if (leftKnob.fingerId != -1 && !leftKnob.active) {
//            println("Stop tracking finger for left knob: fingerId=${leftKnob.fingerId}")
            leftKnobImage.x = leftKnobInitX.toDouble()
            leftKnobImage.y = leftKnobInitY.toDouble()
            leftKnob.fingerId = -1
            lx = 0f
            ly = 0f

            setLeftKnobDigital(0f, 0f)
        }
        if (!rightKnob.active) {
            // TODO
            rightKnob.fingerId = -1
            rx = 0f
            ry = 0f
        }

        // Pass the current input values to the player input system
        // TODO
    }
    fun onTouchMove(fingerId: Int, x: Float, y: Float) {
        if (leftKnob.fingerId == fingerId) {
            // Update left knob position based on movement delta from start position
            leftKnob.fingerId = fingerId
            val knobPosX = (x - leftKnobPivotX).clamp(-leftJoystickRadius, leftJoystickRadius)
            val knobPosY = (y - leftKnobPivotY).clamp(-leftJoystickRadius, leftJoystickRadius)
            leftKnobImage.x = (leftKnobInitX + knobPosX).toDouble()
            leftKnobImage.y = (leftKnobInitY + knobPosY).toDouble()
            lx = knobPosX / leftJoystickRadius
            ly = -knobPosY / leftJoystickRadius
            leftKnob.active = true

            setLeftKnobDigital(lx, ly)

            println("left knob: (${lx.roundDecimalPlaces(2)}, ${ly.roundDecimalPlaces(2)})")
            println("digital left: $left, digital right: $right")
        } else if (rightKnob.fingerId == fingerId) {
            // Update right knob position based on movement delta from start position
            // TODO
            rightKnob.active = true
        } else {
            // Check if this is a new touch for left or right knob based on initial touch position
            if (leftKnob.fingerId == -1 && x > leftKnobX1 && x < leftKnobX2 && y > leftKnobY1 && y < leftKnobY2) {
                println("Start tracking new finger for left knob: fingerId=$fingerId, x=$x, y=$y")
                // Start tracking this finger for the left knob
                leftKnob.fingerId = fingerId
                val knobPosX = (x - leftKnobPivotX).clamp(-leftJoystickRadius, leftJoystickRadius)
                val knobPosY = (y - leftKnobPivotY).clamp(-leftJoystickRadius, leftJoystickRadius)
                leftKnobImage.x = (leftKnobInitX + knobPosX).toDouble()
                leftKnobImage.y = (leftKnobInitY + knobPosY).toDouble()
                lx = knobPosX / leftJoystickRadius
                ly = -knobPosY / leftJoystickRadius
                //leftKnob.startX = x
                //leftKnob.startY = y
                leftKnob.active = true

                setLeftKnobDigital(lx, ly, true)

            } else if (rightKnob.fingerId == -1 /*&& x > rightKnobImage.x && x < rightJoystickImage.x + rightJoystickImage.width &&
                                                   y > rightKnobImage.y && y < rightJoystickImage.y + rightJoystickImage.height*/) {
                // Start tracking this finger for the right knob
                rightKnob.fingerId = fingerId
                //rightKnob.startX = x
                //rightKnob.startY = y
            }
        }
    }

    private fun setLeftKnobDigital(lx: Float, ly: Float, justTrigger: Boolean = false) {
        if (lx > leftKnobTrigger) {
            right = true
            if (justTrigger) justRight = true
            left = false
        } else if (lx < -leftKnobTrigger) {
            right = false
            left = true
            if (justTrigger) justLeft = true
        } else {
            right = false
            left = false
        }

        if (ly > leftKnobTrigger) {
            up = true
            if (justTrigger) justUp = true
            down = false
        } else if (ly < -leftKnobTrigger) {
            up = false
            down = true
            if (justTrigger) justDown = true
        } else {
            up = false
            down = false
        }
    }

    private val leftJoystickImage = image(assetStore.getBitmapTexture("game_pad_joystick_background")) {
        scale = knobScale.toDouble()
        x = leftKnobOffsetX.toDouble()
        y = AppConfig.TARGET_VIRTUAL_HEIGHT - (scaledWidth) - leftKnobOffsetY.toDouble()
        alphaF = 0.5f
        smoothing = false
    }

    private val leftKnobImage = image(assetStore.getBitmapTexture("game_pad_joystick_knob")) {
        scale = knobScale.toDouble()
        alphaF = 0.5f
        smoothing = false
    }

    // value which defines from which point the movement of the left knob should trigger a movement of the player character, can
    // be used to make it less sensitive and avoid accidental movements when the user just wants to rest the finger on the joystick
    var leftKnobTrigger = 0.5f  // [0..1]

    var joystickSizeFactor = 0.75
        set(value) {
            field = value
            leftJoystickRadius = (leftJoystickImage.width * knobScale / 2 * joystickSizeFactor).toFloat()
            leftKnobX1 = leftKnobPivotX - leftJoystickRadius
            leftKnobX2 = leftKnobPivotX + leftJoystickRadius
            leftKnobY1 = leftKnobPivotY - leftJoystickRadius
            leftKnobY2 = leftKnobPivotY + leftJoystickRadius
        }

    // Middle points of the joystick background image, which is the pivot point for the knob movement
    private var leftJoystickRadius: Float = (leftJoystickImage.width * knobScale / 2 * joystickSizeFactor).toFloat()
    private val leftKnobPivotX: Float = (leftJoystickImage.x + leftJoystickImage.width * knobScale / 2).toFloat()
    private val leftKnobPivotY: Float = (leftJoystickImage.y + leftJoystickImage.height * knobScale / 2).toFloat()
    private val leftKnobInitX:Float = ((leftJoystickImage.width - leftKnobImage.width) * knobScale / 2 + leftJoystickImage.x).toFloat()
    private val leftKnobInitY:Float = ((leftJoystickImage.height - leftKnobImage.height) * knobScale / 2 + leftJoystickImage.y).toFloat()
    private var leftKnobX1: Float = leftKnobPivotX - leftJoystickRadius
    private var leftKnobX2: Float = leftKnobPivotX + leftJoystickRadius
    private var leftKnobY1: Float = leftKnobPivotY - leftJoystickRadius
    private var leftKnobY2: Float = leftKnobPivotY + leftJoystickRadius

    // For DEBUGGING left knob movement with keyboard input
    private var isPressedKeyW = false
    private var isPressedKeyA = false
    private var isPressedKeyS = false
    private var isPressedKeyD = false

    init {
        println("Knob bounds: x1=$leftKnobX1, x2=$leftKnobX2, y1=$leftKnobY1, y2=$leftKnobY2")

        // Set the initial position and alpha value for each character of the logo
//        joystickBackground.alpha(0f)
//        joystickKnob.alpha(0f)
        leftKnobImage.x = leftKnobInitX.toDouble()
        leftKnobImage.y = leftKnobInitY.toDouble()

        keys {
            // TESTING - game pad input system
            justDown(Key.W) {
                leftKnobImage.y = (leftKnobInitY - leftJoystickRadius).toDouble()
                ly = 1f
                isPressedKeyW = true
                up = true
                justUp = true
            }
            up(Key.W) {
                if (!isPressedKeyS) {
                    leftKnobImage.y = leftKnobInitY.toDouble()
                    ly = 0f
                } else {
                    leftKnobImage.y = (leftKnobInitY + leftJoystickRadius).toDouble()
                    ly = -1f
                    down = true
                }
                isPressedKeyW = false
                up = false
            }
            justDown(Key.A) {
                leftKnobImage.x = (leftKnobInitX - leftJoystickRadius).toDouble()
                lx = -1f
                isPressedKeyA = true
                left = true
                justLeft = true
            }
            up(Key.A) {
                if (!isPressedKeyD) {
                    leftKnobImage.x = leftKnobInitX.toDouble()
                    lx = 0f
                } else {
                    leftKnobImage.x = (leftKnobInitX + leftJoystickRadius).toDouble()
                    lx = 1f
                    right = true
                }
                isPressedKeyA = false
                left = false
            }
            justDown(Key.S) {
                leftKnobImage.y = (leftKnobInitY + leftJoystickRadius).toDouble()
                ly = -1f
                isPressedKeyS = true
                down = true
                justDown = true
            }
            up(Key.S) {
                if (!isPressedKeyW) {
                    leftKnobImage.y = leftKnobInitY.toDouble()
                    ly = 0f
                } else {
                    leftKnobImage.y = (leftKnobInitY - leftJoystickRadius).toDouble()
                    ly = 1f
                    up = true
                }
                isPressedKeyS = false
                down = false
            }
            justDown(Key.D) {
                leftKnobImage.x = (leftKnobInitX + leftJoystickRadius).toDouble()
                lx = 1f
                isPressedKeyD = true
                right = true
                justRight = true
            }
            up(Key.D) {
                if (!isPressedKeyA) {
                    leftKnobImage.x = leftKnobInitX.toDouble()
                    lx = 0f
                } else {
                    leftKnobImage.x = (leftKnobInitX - leftJoystickRadius).toDouble()
                    lx = -1f
                    left = true
                }
                isPressedKeyD = false
                right = false
            }
        }
    }

    /*
        fun fadeIn(delay: Duration = Duration.NIL) {
            animator {
                wait(delay)
                block { imageButton.visible = true }  // activate button to respond to (touch) presses
                tween(
                    imageButton::alpha[1f].duration(duration = alphaInDuration).easing(alphaInEasing),
                    time = alphaInDuration  // set overall time
                )
                block { pressed = false }  // make button pressable again
            }
        }

        fun fadeOut(delay: Duration = Duration.NIL) {
            animator {
                wait(delay)
                tween(
                    imageButton::alpha[0f].duration(duration = alphaOutDuration).easing(alphaOutEasing),
                    time = alphaOutDuration  // set overall time
                )
                block { imageButton.visible = false }  // deactivate button to NOT respond to (touch) presses - this enables other back buttons in same place to receive input
            }
        }
    */
    init {
    }
}



class PlayerInputSystem() : IntervalSystem(
    interval = Fixed(1 / 60f)
) {
    // directions of digital joystick
    var up = false
    var down = false
    var right = false
    var left = false
    var justUp = false
    var justDown = false
    var justRight = false
    var justLeft = false
    var joystickXDirection = 0.0f
    var joystickYDirection = 0.0f
    var attack = false
    var justReleasedAttack = false
    var attackDirection = 0.0f
    var attackIndex = 0

    // Input variables - will be set by joystick handler (Korge-virtual-joystick ?? addon)
    var isPressedKeyA: Boolean = false
    var isJustPressedKeyA: Boolean = false
    var isPressedKeyS: Boolean = false
    var isJustPressedKeyS: Boolean = false
    var isPressedKeyD: Boolean = false
    var isJustPressedKeyD: Boolean = false
    var isPressedKeyW: Boolean = false
    var isJustPressedKeyW: Boolean = false

    override fun onTick() {
        // get input from user via keyboard
        val newRight = isPressedKeyD  // input.isKeyPressed(Input.Keys.RIGHT) || input.isKeyPressed(Input.Keys.D)
        val newLeft = isPressedKeyA   // input.isKeyPressed(Input.Keys.LEFT) || input.isKeyPressed(Input.Keys.A)
        val newUp = isPressedKeyW     // input.isKeyPressed(Input.Keys.UP) || input.isKeyPressed(Input.Keys.W)
        val newDown = isPressedKeyS   // input.isKeyPressed(Input.Keys.DOWN) || input.isKeyPressed(Input.Keys.S)

        // only set just pressed state if the user is not still pressing the opposite key
        justRight = !(left && newLeft) && isJustPressedKeyD   // (input.isKeyJustPressed(Input.Keys.RIGHT) || input.isKeyJustPressed(Input.Keys.D))
        justLeft = !(right && newRight) && isJustPressedKeyA  // (input.isKeyJustPressed(Input.Keys.LEFT) || input.isKeyJustPressed(Input.Keys.A))
        justUp = !(down && newDown) && isJustPressedKeyW      // (input.isKeyJustPressed(Input.Keys.UP) || input.isKeyJustPressed(Input.Keys.W))
        justDown = !(up && newUp) && isJustPressedKeyS        // (input.isKeyJustPressed(Input.Keys.DOWN) || input.isKeyJustPressed(Input.Keys.S))

        // Reset just pressed keys
        isJustPressedKeyD = false
        isJustPressedKeyA = false
        isJustPressedKeyW = false
        isJustPressedKeyS = false

        // newly pressed keys does not fire if the other opposite key is still pressed
        // this is checked with the first if for horizontal and vertical key state
        if (right && newRight || left && newLeft) {
            // keep right or left when the specific key is still pressed
        } else if (newRight) {
            right = true
            left = false
        } else if (newLeft) {
            right = false
            left = true
        } else {
            left = false
            right = left
        }
        if (up && newUp || down && newDown) {
            // keep right or left when the specific key is still pressed
        } else if (newUp) {
            up = true
            down = false
        } else if (newDown) {
            up = false
            down = true
        } else {
            down = false
            up = false
        }

// TODO        // Get attack button input
//        if (Gdx.input.isTouched) {
//            attack = true
//            // Get new position for attack button
//            var y = startMouseY - Gdx.input.y
//            val maxMovement = 100
//            if (y > maxMovement) {
//                y = maxMovement
//            } else if (y < -maxMovement) {
//                y = -maxMovement
//            }
//            attackDirection = 1.0f * y / 100
//        } else {
//            justReleasedAttack = attack
//            attack = false
//            // When mouse button is released then reset gun to horizontal state
//            attackDirection = 0.0f
//            startMouseY = Gdx.input.y
//        }
//
//        // Call base class function for generic joystick functionality
//        val attackDir = attackDirection
//
//        // We need to map the attack direction [-1...1] to 17 index frames
//        val maxIndexFrames = 16
//        val midIndexFrame = maxIndexFrames / 2
//        val horizontalAttach = 0.2f
//        val maxAttach = 0.95f
//        val indexStep = (1.0f - horizontalAttach) / midIndexFrame
//        attackIndex = if (attackDir < horizontalAttach && attackDir > -horizontalAttach) {
//            // Set index to mid-point
//            midIndexFrame
//        } else if (attackDir > horizontalAttach) {
//            if (attackDir > maxAttach) {
//                // Adjust index to beginning of frame index
//                0
//            } else {
//                midIndexFrame - (attackDir / indexStep).toInt() + 1
//            }
//        } else {
//            if (attackDir < -maxAttach) {
//                maxIndexFrames
//            } else {
//                maxIndexFrames - (midIndexFrame + (attackDir / indexStep).toInt() + 1)
//            }
//        }
    }
}