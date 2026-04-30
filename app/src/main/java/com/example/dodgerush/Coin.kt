package com.example.dodgerush

import android.graphics.*

class Coin {

    var lane = (0..2).random()
    var y = 0f
    private val size = 30f

    fun update(speed: Float) {
        y += speed
    }

    fun draw(canvas: Canvas, screenWidth: Int) {
        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2

        val paint = Paint()
        paint.color = Color.YELLOW

        canvas.drawCircle(x, y, size, paint)
    }

    fun rect(screenWidth: Int): RectF {
        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2

        return RectF(x - size, y - size, x + size, y + size)
    }
}
