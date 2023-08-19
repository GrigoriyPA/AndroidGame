package com.example.android_game.controls

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.android_game.controls.control_elements.ControlElement

// Linear independent touch event forwarder
class SwipeForwarder(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Controls
    private val controls = ArrayList<ControlElement>()

    // Forwarder state
    var listen = true


    // Adding new control element
    fun addControlElement(controlElement: ControlElement) {
        controls.add(controlElement)
    }

    // Touch event forwarder
    private fun computeEvent(control: ControlElement, event: MotionEvent): Boolean {
        var computed = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                computed = control.actionDownEvent(event)
            }

            MotionEvent.ACTION_MOVE -> {
                if (listen) {
                    control.actionMoveEvent(event)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                computed = control.actionUpEvent(event)
            }

            MotionEvent.ACTION_CANCEL -> {
                control.actionCancelEvent(event)
            }
        }

        return computed
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()

        var computedEvent = false
        for (control in controls) {
            if (!computeEvent(control, event)) {
                continue
            }

            computedEvent = true
            if (control.isFinal()) {
                break
            }
        }

        return computedEvent
    }

    override fun performClick(): Boolean {
        super.performClick();
        return true
    }
}
