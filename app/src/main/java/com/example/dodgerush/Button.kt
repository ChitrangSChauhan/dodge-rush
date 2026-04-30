package com.example.dodgerush

import android.graphics.*

class Button(
    val text: String,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {

    fun draw(canvas: Canvas) {
        val paint = Paint()

        // button background
        paint.color = Color.DKGRAY
        canvas.drawRect(x, y, x + width, y + height, paint)

        // border
        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.strokeWidth = 5f
        canvas.drawRect(x, y, x + width, y + height, paint)

        // text
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText(text, x + width / 2, y + height / 2 + 15, paint)
    }

    fun isClicked(touchX: Float, touchY: Float): Boolean {
        return touchX >= x && touchX <= x + width &&
               touchY >= y && touchY <= y + height
    }
}
