package com.example.android_game.controls.control_elements.click

import com.example.android_game.controls.control_elements.ControlElement
import com.example.android_game.utils.math.Vec2

class ClickControl(private val clickView: ClickView) : ControlElement() {
    // ClickControl state
    private var startTimeTracker = 0F

    // Event computer implementation
    override fun actionDownEvent(parentPosition: Vec2, eventTime: Float): Boolean {
        startTimeTracker = eventTime
        return true
    }

    override fun actionMoveEvent(parentPosition: Vec2) {
    }

    override fun actionUpEvent(eventTime: Float) {
        clickView.addBullet(eventTime - startTimeTracker)
    }
}
