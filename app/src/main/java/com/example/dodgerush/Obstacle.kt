package com.example.dodgerush

import android.graphics.*
import kotlin.random.Random

class Obstacle {

    var lane = (0..2).random()
    var y = 0f

    private val width = 100f
    private val height = 160f

    private val colors = listOf(Color.YELLOW, Color.GREEN, Color.BLUE)
    private val color = colors[Random.nextInt(colors.size)]

    fun update(speed: Float, screenWidth: Int) {
        y += speed
    }

    fun draw(canvas: Canvas, screenWidth: Int) {
        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2 - width / 2

        val paint = Paint()
        paint.color = color

        canvas.drawRect(x, y, x + width, y + height, paint)
    }

    fun rect(screenWidth: Int): RectF {
        val laneWidth = screenWidth / 3f
        val x = lane * laneWidth + laneWidth / 2 - width / 2

        return RectF(x, y, x + width, y + height)
    }
}
