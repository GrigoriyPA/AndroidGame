package com.example.android_game.controls.control_elements.joystick

import com.example.android_game.controls.control_elements.ControlElement
import com.example.android_game.utils.math.Vec2

// One touch independent joystick controller
class JoystickControl(private val joystickView: JoystickView) : ControlElement() {
    // Event computer implementation
    override fun actionDownEvent(parentPosition: Vec2, eventTime: Float): Boolean {
        val viewPosition = joystickView.parentCoordinatesToViewCoordinates(parentPosition)
        if (!joystickView.inMainCircle(viewPosition)) {
            return false
        }

        joystickView.updateJoystickPositionFromViewPosition(viewPosition)

        return true
    }

    override fun actionMoveEvent(parentPosition: Vec2) {
        val viewPosition = joystickView.parentCoordinatesToViewCoordinates(parentPosition)
        joystickView.updateJoystickPositionFromViewPosition(viewPosition)
    }

    override fun actionUpEvent(eventTime: Float) {
        joystickView.dropJoystickState()
    }

    // Event computer description
    override fun isFinal() = true
}
