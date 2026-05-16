package com.AEAS.dailymemory

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SimonPhase { INTRO, SYSTEM_PLAYING, WAITING_PLAYER, GAMEOVER_WIN, GAMEOVER_LOSE }

class SimonGameViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(SimonPhase.INTRO)
    val phase: StateFlow<SimonPhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _sequence = MutableStateFlow<List<Int>>(emptyList())
    val sequence: StateFlow<List<Int>> = _sequence.asStateFlow()

    private val _activeButton = MutableStateFlow<Int?>(null)
    val activeButton: StateFlow<Int?> = _activeButton.asStateFlow()

    var currentDifficulty = "Fácil"
    var numButtons = 4
    var targetRound = 50
    var speedBase = 900L

    private var playerPosition = 0
    var isSoundEnabled = true

    val colorsEasy = listOf(Color(0xFF28CF5F), Color(0xFFF54F4F), Color(0xFFFFD43B), Color(0xFF2FB7FF))
    val colorsMedium = listOf(Color(0xFF28CF5F), Color(0xFFF54F4F), Color(0xFFFFD43B), Color(0xFF2FB7FF), Color(0xFFB06CFF))
    val colorsHard = listOf(Color(0xFF2FE27A), Color(0xFFFF5B5B), Color(0xFFFFD648), Color(0xFF2DB6FF), Color(0xFFC86DFF), Color(0xFFFF8A4B))

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        when (difficulty) {
            "Fácil" -> { numButtons = 4; targetRound = 50; speedBase = 900L }
            "Medio" -> { numButtons = 5; targetRound = 100; speedBase = 800L }
            "Difícil" -> { numButtons = 6; targetRound = 150; speedBase = 700L }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _sequence.value = emptyList()
        playerPosition = 0
        _phase.value = SimonPhase.INTRO
    }

    fun startGame() {
        resetStats()
        addStepAndPlay()
    }

    private fun addStepAndPlay() {
        val nextStep = (0 until numButtons).random()
        _sequence.value = _sequence.value + nextStep
        _score.value = maxOf(0, _sequence.value.size - 1)
        playSequence()
    }

    private fun playSequence() {
        _phase.value = SimonPhase.SYSTEM_PLAYING
        playerPosition = 0

        viewModelScope.launch {
            delay(800)

            val reduction = _sequence.value.size * if (numButtons == 4) 40L else if (numButtons == 5) 35L else 30L
            val speed = maxOf(200L, speedBase - reduction)

            for (step in _sequence.value) {
                _activeButton.value = step
                delay(speed / 2)
                _activeButton.value = null
                delay(speed / 2)
            }

            _phase.value = SimonPhase.WAITING_PLAYER
        }
    }

    fun onPlayerInput(index: Int) {
        if (_phase.value != SimonPhase.WAITING_PLAYER) return

        viewModelScope.launch {
            _activeButton.value = index
            delay(250)
            _activeButton.value = null
        }

        if (index != _sequence.value[playerPosition]) {
            _phase.value = SimonPhase.GAMEOVER_LOSE
            saveScoreToFirebase()
            return
        }

        playerPosition++

        if (playerPosition >= _sequence.value.size) {
            _score.value = _sequence.value.size

            if (_score.value >= targetRound) {
                _phase.value = SimonPhase.GAMEOVER_WIN
                saveScoreToFirebase()
            } else {
                _phase.value = SimonPhase.SYSTEM_PLAYING
                viewModelScope.launch {
                    delay(1000)
                    addStepAndPlay()
                }
            }
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Ecoica",
            "gameName" to "Simon",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}