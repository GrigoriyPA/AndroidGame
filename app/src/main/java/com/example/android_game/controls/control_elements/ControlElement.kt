package com.example.android_game.controls.control_elements

import android.view.MotionEvent
import com.example.android_game.utils.math.Vec2

// One touch independent controller
abstract class ControlElement {
    // Control element state
    private var activated = false
    private var pointerId = 0

    // Event forwarders
    fun actionDownEvent(event: MotionEvent): Boolean {
        if (activated) {
            return false
        }

        val index = event.actionIndex
        val computed = actionDownEvent(Vec2(event.getX(index), event.getY(index)), event.eventTime.toFloat() / 1000F)

        if (computed) {
            activated = true
            pointerId = event.getPointerId(index)
        }

        return computed
    }

    fun actionMoveEvent(event: MotionEvent) {
        if (!activated) {
            return
        }

        for (index in 0 until event.pointerCount) {
            if (event.getPointerId(index) != pointerId) {
                continue
            }

            actionMoveEvent(Vec2(event.getX(index), event.getY(index)))
            break
        }
    }

    fun actionUpEvent(event: MotionEvent): Boolean {
        if (!activated || event.getPointerId(event.actionIndex) != pointerId) {
            return false
        }

        actionUpEvent(event.eventTime.toFloat() / 1000F)
        activated = false

        return true
    }

    fun actionCancelEvent(event: MotionEvent) {
        if (activated) {
            actionUpEvent(event.eventTime.toFloat() / 1000F)
            activated = false
        }
    }


    // Control description
    open fun isFinal() = false

    // Event handlers
    abstract fun actionDownEvent(parentPosition: Vec2, eventTime: Float): Boolean
    abstract fun actionMoveEvent(parentPosition: Vec2)
    abstract fun actionUpEvent(eventTime: Float)
}
