package com.example.dodgerush

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Obstacle {

    var lane = (0..2).random()
    var y = -220f

    private val width = 140f
    private val height = 220f

    private val colors = listOf(
        Color.YELLOW,
        Color.GREEN,
        Color.BLUE
    )

    private val color = colors.random()

    fun update(speed: Float, screenWidth: Int) {
        y += speed
    }

    fun draw(canvas: Canvas, screenWidth: Int) {

        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2 - width / 2

        val paint = Paint()

        val rect = RectF(
            x,
            y,
            x + width,
            y + height
        )

        paint.color = color
        canvas.drawRoundRect(rect, 25f, 25f, paint)

        // windshield
        paint.color = Color.WHITE
        canvas.drawRoundRect(
            x + 20,
            y + 40,
            x + width - 20,
            y + 120,
            15f,
            15f,
            paint
        )

        // wheels
        paint.color = Color.BLACK

        canvas.drawRect(x - 10, y + 30, x + 10, y + 80, paint)
        canvas.drawRect(x - 10, y + 140, x + 10, y + 190, paint)

        canvas.drawRect(x + width - 10, y + 30, x + width + 10, y + 80, paint)
        canvas.drawRect(x + width - 10, y + 140, x + width + 10, y + 190, paint)
    }

    fun rect(screenWidth: Int): RectF {

        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2 - width / 2

        return RectF(x, y, x + width, y + height)
    }
}
