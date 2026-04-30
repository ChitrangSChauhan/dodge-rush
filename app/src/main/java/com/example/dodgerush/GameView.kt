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

    // UI Buttons
    private lateinit var playButton: Button
    private lateinit var shopButton: Button
    private lateinit var restartButton: Button
    private lateinit var backButton: Button
    private lateinit var upgradeButton: Button
    private lateinit var nextCarButton: Button

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setupButtons()
        running = true
        thread = Thread { gameLoop() }
        thread?.start()
    }

    private fun setupButtons() {
        val centerX = width / 2f

        playButton = Button("PLAY", centerX - 200f, 800f, 400f, 120f)
        shopButton = Button("SHOP", centerX - 200f, 950f, 400f, 120f)

        restartButton = Button("RESTART", centerX - 200f, 900f, 400f, 120f)
        backButton = Button("BACK", centerX - 200f, 1100f, 400f, 120f)

        upgradeButton = Button("UPGRADE SPEED", centerX - 250f, 700f, 500f, 120f)
        nextCarButton = Button("NEXT CAR", centerX - 200f, 900f, 400f, 120f)
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

        repeat(20) {
            particles.add(Particle(player.x, player.y))
        }
    }

    private fun update() {
        if (gameState != GameState.PLAYING) return

        score++
        gameSpeed += speedIncrease * gameData.getSpeedLevel()

        player.update(width)

        if (Math.random() < 0.05) obstacles.add(Obstacle())
        if (Math.random() < 0.02) coins.add(Coin())

        obstacles.forEach { it.update(gameSpeed, width) }
        coins.forEach { it.update(gameSpeed) }

        obstacles.forEach {
            if (RectF.intersects(player.rect(), it.rect(width))) {
                gameOver()
            }
        }

        coins.removeAll {
            if (RectF.intersects(player.rect(), it.rect(width))) {
                coinScore++
                true
            } else false
        }

        particles.forEach { it.update() }
        particles.removeAll { !it.isAlive() }
    }

    private fun drawGame(canvas: Canvas) {

        val paint = Paint()

        canvas.drawColor(Color.BLACK)

        when (gameState) {

            GameState.START -> {
                paint.color = Color.WHITE
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText("DODGE RUSH", width / 2f, 400f, paint)

                playButton.draw(canvas)
                shopButton.draw(canvas)
            }

            GameState.PLAYING -> {

                // road
                paint.color = Color.DKGRAY
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                val laneWidth = width / 3f
                paint.color = Color.WHITE
                canvas.drawLine(laneWidth, 0f, laneWidth, height.toFloat(), paint)
                canvas.drawLine(laneWidth * 2, 0f, laneWidth * 2, height.toFloat(), paint)

                player.draw(canvas)

                obstacles.forEach { it.draw(canvas, width) }
                coins.forEach { it.draw(canvas, width) }
                particles.forEach { it.draw(canvas) }

                paint.textSize = 50f
                canvas.drawText("Score: $score", 50f, 80f, paint)
                canvas.drawText("Coins: $coinScore", 50f, 140f, paint)
            }

            GameState.GAME_OVER -> {
                paint.color = Color.WHITE
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText("GAME OVER", width / 2f, 400f, paint)
                canvas.drawText("Score: $score", width / 2f, 500f, paint)
                canvas.drawText("Coins: ${gameData.getCoins()}", width / 2f, 600f, paint)

                restartButton.draw(canvas)
                shopButton.draw(canvas)
            }

            GameState.SHOP -> {
                paint.color = Color.WHITE
                paint.textSize = 70f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText("SHOP", width / 2f, 300f, paint)
                canvas.drawText("Coins: ${gameData.getCoins()}", width / 2f, 400f, paint)

                // current car preview
                player.draw(canvas)

                nextCarButton.draw(canvas)
                upgradeButton.draw(canvas)
                backButton.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_DOWN) return true

        val x = event.x
        val y = event.y

        when (gameState) {

            GameState.START -> {
                if (playButton.isClicked(x, y)) startGame()
                if (shopButton.isClicked(x, y)) gameState = GameState.SHOP
            }

            GameState.PLAYING -> {
                if (x < width / 2) player.moveLeft()
                else player.moveRight()
            }

            GameState.GAME_OVER -> {
                if (restartButton.isClicked(x, y)) startGame()
                if (shopButton.isClicked(x, y)) gameState = GameState.SHOP
            }

            GameState.SHOP -> {

                if (backButton.isClicked(x, y)) gameState = GameState.START

                if (nextCarButton.isClicked(x, y)) {
                    val next = (gameData.getSelectedCar() + 1) % 4
                    if (gameData.isCarUnlocked(next)) {
                        gameData.setSelectedCar(next)
                    } else if (gameData.getCoins() >= 50) {
                        gameData.unlockCar(next)
                        gameData.setSelectedCar(next)
                        gameData.addCoins(-50)
                    }
                }

                if (upgradeButton.isClicked(x, y)) {
                    if (gameData.getCoins() >= 30) {
                        gameData.upgradeSpeed()
                        gameData.addCoins(-30)
                    }
                }
            }
        }

        return true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        thread?.join()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
}
