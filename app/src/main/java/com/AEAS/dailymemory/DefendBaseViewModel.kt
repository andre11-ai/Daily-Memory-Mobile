package com.AEAS.dailymemory

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class DefendPhase { INTRO, PLAYING, GAMEOVER_WIN, GAMEOVER_LOSE }

data class EnemyCircle(
    val id: Int,
    val spawnTime: Long,
    val angle: Float,
    val color: Color
)

class DefendBaseViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(DefendPhase.INTRO)
    val phase: StateFlow<DefendPhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    // --- AGREGADO: Control de Vidas ---
    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _enemies = MutableStateFlow<List<EnemyCircle>>(emptyList())
    val enemies: StateFlow<List<EnemyCircle>> = _enemies.asStateFlow()

    var currentDifficulty = "Fácil"
    var targetScore = 20

    var timeToReachCenterMs = 6000L
    private var spawnIntervalMs = 1500L

    private var gameJob: Job? = null
    private var enemyIdCounter = 0
    private var lastSpawnTime = 0L

    private val enemyColors = listOf(
        Color(0xFFF54F4F), Color(0xFF2FB7FF), Color(0xFFFFD43B),
        Color(0xFF28CF5F), Color(0xFFB06CFF), Color(0xFFFF8A4B)
    )

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        when (difficulty) {
            "Fácil" -> { targetScore = 20; spawnIntervalMs = 1500L; timeToReachCenterMs = 6000L }
            "Medio" -> { targetScore = 40; spawnIntervalMs = 1000L; timeToReachCenterMs = 4500L }
            "Difícil" -> { targetScore = 60; spawnIntervalMs = 600L; timeToReachCenterMs = 3000L }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _lives.value = 3 // Reiniciar vidas
        _enemies.value = emptyList()
        enemyIdCounter = 0
        lastSpawnTime = 0L
        _phase.value = DefendPhase.INTRO
        gameJob?.cancel()
    }

    fun startGame() {
        resetStats()
        _phase.value = DefendPhase.PLAYING
        lastSpawnTime = System.currentTimeMillis()
        startGameLoop()
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (_phase.value == DefendPhase.PLAYING) {
                delay(32)
                val currentTime = System.currentTimeMillis()
                var listChanged = false
                val currentEnemies = _enemies.value.toMutableList()

                if (currentTime - lastSpawnTime >= spawnIntervalMs) {
                    val randomAngle = (0..360).random() * PI / 180.0
                    currentEnemies.add(
                        EnemyCircle(
                            id = enemyIdCounter++,
                            spawnTime = currentTime,
                            angle = randomAngle.toFloat(),
                            color = enemyColors.random()
                        )
                    )
                    lastSpawnTime = currentTime
                    listChanged = true
                }

                val iterator = currentEnemies.iterator()
                var missedCount = 0

                while (iterator.hasNext()) {
                    val enemy = iterator.next()
                    val aliveTime = currentTime - enemy.spawnTime

                    // --- MODIFICADO: Pierde vida en lugar de Game Over inmediato ---
                    if (aliveTime >= timeToReachCenterMs) {
                        iterator.remove()
                        missedCount++
                        listChanged = true
                    }
                }

                // Aplicar daño
                if (missedCount > 0) {
                    _lives.value -= missedCount
                    if (_lives.value <= 0) {
                        _enemies.value = currentEnemies
                        endGame(win = false)
                        return@launch
                    }
                }

                if (listChanged) {
                    _enemies.value = currentEnemies
                }
            }
        }
    }

    fun onPlayerTap(logicalX: Float, logicalY: Float, currentTime: Long) {
        if (_phase.value != DefendPhase.PLAYING) return

        val currentEnemies = _enemies.value.toMutableList()
        var hitIndex = -1

        val spawnDistance = 1.2f

        for (i in currentEnemies.indices) {
            val enemy = currentEnemies[i]
            val aliveTime = (currentTime - enemy.spawnTime).coerceAtLeast(0L)
            val progress = aliveTime.toFloat() / timeToReachCenterMs.toFloat()
            val currentDistance = spawnDistance * (1f - progress)

            val currentX = cos(enemy.angle) * currentDistance
            val currentY = sin(enemy.angle) * currentDistance

            val dist = sqrt((currentX - logicalX) * (currentX - logicalX) + (currentY - logicalY) * (currentY - logicalY))

            if (dist < 0.25f) {
                hitIndex = i
                break
            }
        }

        if (hitIndex != -1) {
            currentEnemies.removeAt(hitIndex)
            _score.value += 1
            _enemies.value = currentEnemies

            if (_score.value >= targetScore) {
                endGame(win = true)
            }
        }
    }

    private fun endGame(win: Boolean) {
        gameJob?.cancel()
        _phase.value = if (win) DefendPhase.GAMEOVER_WIN else DefendPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        startGame()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Muscular",
            "gameName" to "Defiende la Base",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}