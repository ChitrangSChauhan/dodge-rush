package com.example.dodgerush

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Obstacle {

    var x = (50..1000).random().toFloat()
    var y = 0f
    private val size = 80f

    fun update() {
        y += 15
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.RED
        canvas.drawRect(x, y, x + size, y + size, paint)
    }

    fun rect(): RectF {
        return RectF(x, y, x + size, y + size)
    }
}
