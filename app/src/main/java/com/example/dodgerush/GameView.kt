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

    // lane tracking
    private val laneOccupied = BooleanArray(3) { false }

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

        laneOccupied.fill(false)

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

        // reset lane tracking
        laneOccupied.fill(false)

        // mark occupied lanes
        for (obs in obstacles) {
            if (obs.y < height) {
                laneOccupied[obs.lane] = true
            }
        }

        // spawn obstacle in free lane
        if (Math.random() < 0.05) {
            val freeLanes = (0..2).filter { !laneOccupied[it] }
            if (freeLanes.isNotEmpty()) {
                val lane = freeLanes[(Math.random() * freeLanes.size).toInt()]
                val obs = Obstacle()
                obs.lane = lane
                obstacles.add(obs)
            }
        }

        // spawn coin in free lane
        if (Math.random() < 0.02) {
            val freeLanes = (0..2).filter { !laneOccupied[it] }
            if (freeLanes.isNotEmpty()) {
                val lane = freeLanes[(Math.random() * freeLanes.size).toInt()]
                val coin = Coin()
                coin.lane = lane
                coins.add(coin)
            }
        }

        // update objects
        obstacles.forEach { it.update(gameSpeed, width) }
        coins.forEach { it.update(gameSpeed) }

        // remove off screen
        obstacles.removeAll { it.y > height }
        coins.removeAll { it.y > height }

        // collision
        for (obs in obstacles) {
            if (RectF.intersects(player.rect(), obs.rect(width))) {
                gameOver()
            }
        }

        // coin collection
        coins.removeAll {
            if (RectF.intersects(player.rect(), it.rect(width))) {
                coinScore++
                true
            } else false
        }

        // particles
        particles.forEach { it.update() }
        particles.removeAll { !it.isAlive() }
    }

    private fun drawGame(canvas: Canvas) {

        // FIXED shake (no .random on float range)
        if (shakeTime > 0) {
            val dx = (Math.random() * shakeIntensity * 2 - shakeIntensity).toFloat()
            val dy = (Math.random() * shakeIntensity * 2 - shakeIntensity).toFloat()
            canvas.translate(dx, dy)
            shakeTime--
        }

        val paint = Paint()

        canvas.drawColor(Color.BLACK)

        when (gameState) {

            GameState.START -> {
                paint.color = Color.WHITE
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText("DODGE RUSH", width / 2f, 400f, paint)
                canvas.drawText("Tap to Start", width / 2f, 600f, paint)
            }

            GameState.PLAYING -> {

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

                canvas.drawText("Tap to Restart", width / 2f, 800f, paint)
            }

            // FIXED missing case
            GameState.SHOP -> {
                paint.color = Color.WHITE
                paint.textSize = 70f
                paint.textAlign = Paint.Align.CENTER

                canvas.drawText("SHOP (Coming Soon)", width / 2f, 400f, paint)
                canvas.drawText("Tap to go back", width / 2f, 600f, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        when (gameState) {

            GameState.START -> startGame()

            GameState.PLAYING -> {
                if (event.x < width / 2) player.moveLeft()
                else player.moveRight()
            }

            GameState.GAME_OVER -> startGame()

            // FIXED missing case
            GameState.SHOP -> {
                gameState = GameState.START
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
