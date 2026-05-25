package com.AEAS.dailymemory

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

enum class FallingPhase { INTRO, PLAYING, GAMEOVER_WIN, GAMEOVER_LOSE }

data class FallingTile(
    val id: Int,
    val xPosition: Float,
    val spawnTime: Long // Usaremos el tiempo de nacimiento para animar fluidamente
)

class FallingTilesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(FallingPhase.INTRO)
    val phase: StateFlow<FallingPhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _tiles = MutableStateFlow<List<FallingTile>>(emptyList())
    val tiles: StateFlow<List<FallingTile>> = _tiles.asStateFlow()

    var currentDifficulty = "Fácil"
    var targetScore = 15

    // Tiempos ajustables
    var fallDurationMs = 4000L // Cuánto tarda en caer de arriba a abajo
    private var spawnIntervalMs = 1200L

    private var gameJob: Job? = null
    private var tileIdCounter = 0
    private var lastSpawnTime = 0L

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        when (difficulty) {
            "Fácil" -> { targetScore = 15; fallDurationMs = 3500L; spawnIntervalMs = 1200L }
            "Medio" -> { targetScore = 25; fallDurationMs = 2500L; spawnIntervalMs = 900L }
            "Difícil" -> { targetScore = 35; fallDurationMs = 1500L; spawnIntervalMs = 600L }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _lives.value = 3
        _tiles.value = emptyList()
        tileIdCounter = 0
        lastSpawnTime = 0L
        _phase.value = FallingPhase.INTRO
        gameJob?.cancel()
    }

    fun startGame() {
        resetStats()
        _phase.value = FallingPhase.PLAYING
        lastSpawnTime = System.currentTimeMillis()
        startGameLoop()
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (_phase.value == FallingPhase.PLAYING) {
                delay(32) // Este ciclo es solo para lógica (no afecta la animación)
                val currentTime = System.currentTimeMillis()

                var listChanged = false
                val currentList = _tiles.value.toMutableList()

                // 1. Aparecer nueva burbuja
                if (currentTime - lastSpawnTime >= spawnIntervalMs) {
                    val randomX = (5..85).random() / 100f // Evita bordes extremos
                    currentList.add(FallingTile(id = tileIdCounter++, xPosition = randomX, spawnTime = currentTime))
                    lastSpawnTime = currentTime
                    listChanged = true
                }

                // 2. Revisar si alguna tocó el suelo
                val iterator = currentList.iterator()
                var missedCount = 0

                while (iterator.hasNext()) {
                    val tile = iterator.next()
                    // Si el tiempo actual sobrepasa su duración de caída (+ un pequeño margen)
                    if (currentTime - tile.spawnTime > fallDurationMs + 100) {
                        iterator.remove()
                        missedCount++
                        listChanged = true
                    }
                }

                if (missedCount > 0) {
                    _lives.value -= missedCount
                    if (_lives.value <= 0) {
                        _tiles.value = currentList
                        endGame(win = false)
                        break
                    }
                }

                // Solo actualizamos el StateFlow si apareció o se borró una burbuja
                if (listChanged) {
                    _tiles.value = currentList
                }
            }
        }
    }

    fun tapTile(id: Int) {
        if (_phase.value != FallingPhase.PLAYING) return

        val currentList = _tiles.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }

        if (index != -1) {
            currentList.removeAt(index)
            _tiles.value = currentList
            _score.value += 1

            if (_score.value >= targetScore) {
                endGame(win = true)
            }
        }
    }

    private fun endGame(win: Boolean) {
        gameJob?.cancel()
        _phase.value = if (win) FallingPhase.GAMEOVER_WIN else FallingPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        startGame()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Muscular",
            "gameName" to "Lluvia de Figuras",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}