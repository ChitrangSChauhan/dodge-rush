package com.example.dodgerush

import android.graphics.*

class Player(var x: Float, var y: Float, private val gameData: GameData) {

    private val width = 100f
    private val height = 160f

    private var lane = 1
    private var targetX = x

    fun update(screenWidth: Int) {
        val laneWidth = screenWidth / 3f
        targetX = lane * laneWidth + laneWidth / 2 - width / 2
        x += (targetX - x) * 0.2f
    }

    fun moveLeft() { if (lane > 0) lane-- }
    fun moveRight() { if (lane < 2) lane++ }

    fun draw(canvas: Canvas) {
        val paint = Paint()

        when (gameData.getSelectedCar()) {
            0 -> paint.color = Color.RED
            1 -> paint.color = Color.CYAN
            2 -> paint.color = Color.MAGENTA
            3 -> paint.color = Color.WHITE
        }

        canvas.drawRect(x, y, x + width, y + height, paint)

        paint.color = Color.BLACK
        canvas.drawRect(x + 20, y + 20, x + width - 20, y + 70, paint)
    }

    fun rect(): RectF = RectF(x, y, x + width, y + height)
}
