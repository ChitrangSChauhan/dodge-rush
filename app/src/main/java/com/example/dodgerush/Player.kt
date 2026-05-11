package com.example.dodgerush

class Player(var x: Float, var y: Float) {

    private var lane = 1

    private val width = 140f
    private val height = 220f

    private val bitmap = BitmapFactory.decodeResource(
        Resources.getSystem(),
        android.R.drawable.sym_def_app_icon
    )

    fun update(screenWidth: Int) {
        val laneWidth = screenWidth / 3f
        x = lane * laneWidth + laneWidth / 2 - width / 2
    }

    fun moveLeft() {
        if (lane > 0) lane--
    }

    fun moveRight() {
        if (lane < 2) lane++
    }

    fun draw(canvas: Canvas) {

        val rect = RectF(
            x,
            y,
            x + width,
            y + height
        )

        val paint = Paint()

        paint.color = Color.RED
        canvas.drawRoundRect(rect, 25f, 25f, paint)

        // windshield
        paint.color = Color.CYAN
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

    fun rect(): RectF {
        return RectF(x, y, x + width, y + height)
    }
}
