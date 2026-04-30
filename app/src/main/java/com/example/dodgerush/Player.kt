package com.example.dodgerush

import android.graphics.*

class Player(var x: Float, var y: Float) {

    private val radius = 40f
    private val speed = 120f

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.GREEN
        canvas.drawCircle(x, y, radius, paint)
    }

    fun moveLeft() {
        x -= speed
        if (x < radius) x = radius
    }

    fun moveRight(screenWidth: Int) {
        x += speed
        if (x > screenWidth - radius) x = screenWidth - radius
    }

    fun rect(): RectF {
        return RectF(x - radius, y - radius, x + radius, y + radius)
    }
}
