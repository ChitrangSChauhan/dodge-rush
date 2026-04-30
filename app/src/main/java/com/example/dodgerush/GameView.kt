package com.example.dodgerush

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private var thread: Thread? = null
    private var running = false

    private var gameState = GameState.START

    private val player = Player(500f, 1400f)
    private val obstacles: MutableList<Obstacle> = mutableListOf()

    private var score = 0
    private var highScore = 0

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread {
            gameLoop()
        }
        thread?.start()
    }

    private fun gameLoop() {
        while (running) {
            if (!holder.surface.isValid) continue

            val canvas = holder.lockCanvas() ?: continue

            update()
            drawGame(canvas)

            holder.unlockCanvasAndPost(canvas)

            Thread.sleep(16)
        }
    }

    private fun startGame() {
        obstacles.clear()
        score = 0
        player.x = width / 2f
        gameState = GameState.PLAYING
    }

    private fun gameOver() {
        gameState = GameState.GAME_OVER
        if (score > highScore) highScore = score
    }

    private fun update() {
        if (gameState != GameState.PLAYING) return

        score++

        if (Math.random() < 0.04) {
            obstacles.add(Obstacle())
        }

        obstacles.forEach { it.update() }

        for (obs in obstacles) {
            if (RectF.intersects(player.rect(), obs.rect())) {
                gameOver()
            }
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER

        when (gameState) {

            GameState.START -> {
                canvas.drawText("DODGE RUSH", width / 2f, height / 2f - 100, paint)
                canvas.drawText("Tap to Start", width / 2f, height / 2f, paint)
            }

            GameState.PLAYING -> {
                player.draw(canvas)
                obstacles.forEach { it.draw(canvas) }

                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("Score: $score", 50f, 80f, paint)
            }

            GameState.GAME_OVER -> {
                canvas.drawText("GAME OVER", width / 2f, height / 2f - 100, paint)
                canvas.drawText("Score: $score", width / 2f, height / 2f, paint)
                canvas.drawText("High Score: $highScore", width / 2f, height / 2f + 100, paint)
                canvas.drawText("Tap to Restart", width / 2f, height / 2f + 200, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (gameState) {
                GameState.START -> startGame()
                GameState.PLAYING -> {
                    if (event.x < width / 2) player.moveLeft()
                    else player.moveRight(width)
                }
                GameState.GAME_OVER -> startGame()
            }
        }
        return true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread?.join()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
}
