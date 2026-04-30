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

    private val gameData = GameData(context)
    private val player = Player(0f, 1400f, gameData)

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<Coin>()
    private val particles = mutableListOf<Particle>()

    private var score = 0
    private var coinScore = 0

    private var gameSpeed = 15f
    private var speedIncrease = 0.01f

    private var shakeTime = 0
    private var shakeIntensity = 0f

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread { gameLoop() }
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
        coins.clear()
        particles.clear()

        score = 0
        coinScore = 0
        gameSpeed = 15f

        gameState = GameState.PLAYING
    }

    private fun gameOver() {
        gameState = GameState.GAME_OVER

        gameData.addCoins(coinScore)

        shakeTime = 20
        shakeIntensity = 20f

        for (i in 0..20) {
            particles.add(Particle(player.x, player.y))
        }
    }

    private fun update() {
        if (gameState != GameState.PLAYING) return

        score++

        val speedMultiplier = gameData.getSpeedLevel()
        gameSpeed += speedIncrease * speedMultiplier

        player.update(width)

        if (Math.random() < 0.05) obstacles.add(Obstacle())
        if (Math.random() < 0.02) coins.add(Coin())

        obstacles.forEach { it.update(gameSpeed, width) }
        coins.forEach { it.update(gameSpeed) }

        for (obs in obstacles) {
            if (RectF.intersects(player.rect(), obs.rect(width))) {
                gameOver()
            }
        }

        for (coin in coins) {
            if (RectF.intersects(player.rect(), coin.rect(width))) {
                coinScore++
                shakeTime = 5
                shakeIntensity = 5f
            }
        }

        particles.forEach { it.update() }
        particles.removeAll { !it.isAlive() }
    }

    private fun drawGame(canvas: Canvas) {

        if (shakeTime > 0) {
            val dx = (-shakeIntensity..shakeIntensity).random()
            val dy = (-shakeIntensity..shakeIntensity).random()
            canvas.translate(dx, dy)
            shakeTime--
        }

        val paint = Paint()

        // Road
        paint.color = Color.DKGRAY
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Lanes
        paint.color = Color.WHITE
        paint.strokeWidth = 8f
        val laneWidth = width / 3f

        canvas.drawLine(laneWidth, 0f, laneWidth, height.toFloat(), paint)
        canvas.drawLine(laneWidth * 2, 0f, laneWidth * 2, height.toFloat(), paint)

        when (gameState) {

            GameState.START -> {
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.WHITE

                canvas.drawText("DODGE RUSH", width / 2f, height / 2f, paint)
                canvas.drawText("Tap to Start", width / 2f, height / 2f + 100, paint)
            }

            GameState.PLAYING -> {
                player.draw(canvas)

                obstacles.forEach { it.draw(canvas, width) }
                coins.forEach { it.draw(canvas, width) }
                particles.forEach { it.draw(canvas) }

                paint.textSize = 50f
                paint.textAlign = Paint.Align.LEFT
                paint.color = Color.WHITE

                canvas.drawText("Score: $score", 50f, 80f, paint)
                canvas.drawText("Coins: $coinScore", 50f, 140f, paint)
            }

            GameState.GAME_OVER -> {
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.WHITE

                canvas.drawText("GAME OVER", width / 2f, height / 2f)
                canvas.drawText("Score: $score", width / 2f, height / 2f + 100)
                canvas.drawText("Coins: ${gameData.getCoins()}", width / 2f, height / 2f + 200)

                canvas.drawText("Left: Change Car", width / 2f, height / 2f + 300)
                canvas.drawText("Right: Upgrade Speed", width / 2f, height / 2f + 400)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when (gameState) {
                GameState.START -> startGame()
                GameState.PLAYING -> {
                    if (event.x < width / 2) player.moveLeft()
                    else player.moveRight()
                }
                GameState.GAME_OVER -> {
                    if (event.x < width / 2) {
                        val nextCar = (gameData.getSelectedCar() + 1) % 4
                        if (gameData.isCarUnlocked(nextCar)) {
                            gameData.setSelectedCar(nextCar)
                        } else if (gameData.getCoins() >= 50) {
                            gameData.unlockCar(nextCar)
                            gameData.setSelectedCar(nextCar)
                            gameData.addCoins(-50)
                        }
                    } else {
                        if (gameData.getCoins() >= 30) {
                            gameData.upgradeSpeed()
                            gameData.addCoins(-30)
                        }
                    }
                }
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
