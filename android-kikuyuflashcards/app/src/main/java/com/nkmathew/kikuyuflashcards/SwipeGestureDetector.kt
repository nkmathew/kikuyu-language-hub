package com.nkmathew.kikuyuflashcards

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeGestureDetector(private val listener: SwipeListener) : GestureDetector.SimpleOnGestureListener() {
    
    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    interface SwipeListener {
        fun onSwipeLeft()
        fun onSwipeRight()
        fun onSwipeUp()
        fun onSwipeDown()
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (e1 == null) return false
        
        val diffY = e2.y - e1.y
        val diffX = e2.x - e1.x
        
        if (abs(diffX) > abs(diffY)) {
            if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    listener.onSwipeRight()
                } else {
                    listener.onSwipeLeft()
                }
                return true
            }
        } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffY > 0) {
                listener.onSwipeDown()
            } else {
                listener.onSwipeUp()
            }
            return true
        }
        
        return false
    }
}