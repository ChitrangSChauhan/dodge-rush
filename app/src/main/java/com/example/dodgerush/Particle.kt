package com.example.dodgerush

import android.graphics.*
import kotlin.random.Random

class Particle(var x: Float, var y: Float) {

    private var dx = Random.nextFloat() * 10 - 5
    private var dy = Random.nextFloat() * 10 - 5
    private var life = 20

    fun update() {
        x += dx
        y += dy
        life--
    }

    fun draw(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.YELLOW
        canvas.drawCircle(x, y, 8f, paint)
    }

    fun isAlive(): Boolean = life > 0
}
