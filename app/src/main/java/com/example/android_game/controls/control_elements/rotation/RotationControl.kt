package com.example.android_game.controls.control_elements.rotation

import com.example.android_game.controls.control_elements.ControlElement
import com.example.android_game.utils.math.Vec2

class RotationControl(private val rotationView: RotationView) : ControlElement() {
    // RotationControl state
    private var xPositionTracker = 0F

    // Event computer implementation
    override fun actionDownEvent(parentPosition: Vec2, eventTime: Float): Boolean {
        xPositionTracker = parentPosition.x
        return true
    }

    override fun actionMoveEvent(parentPosition: Vec2) {
        rotationView.updateRotation(parentPosition.x - xPositionTracker)
        xPositionTracker = parentPosition.x
    }

    override fun actionUpEvent(eventTime: Float) {
    }
}
