package com.AEAS.dailymemory

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GamePhase { INTRO, MEMORIZE, SELECT, GAMEOVER_WIN, GAMEOVER_LOSE }

class ColorGameViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val baseColors = listOf(
        Color(0xFFF44336), Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFEB3B),
        Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4)
    )
    private val extraColors = listOf(Color(0xFF795548), Color(0xFF8BC34A))

    private val _phase = MutableStateFlow(GamePhase.INTRO)
    val phase: StateFlow<GamePhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _colorsToMemorize = MutableStateFlow<List<Color>>(emptyList())
    val colorsToMemorize: StateFlow<List<Color>> = _colorsToMemorize.asStateFlow()

    private val _availableColors = MutableStateFlow<List<Color>>(emptyList())
    val availableColors: StateFlow<List<Color>> = _availableColors.asStateFlow()

    private val _userSelectedColors = MutableStateFlow<List<Color>>(emptyList())
    val userSelectedColors: StateFlow<List<Color>> = _userSelectedColors.asStateFlow()

    var currentDifficulty = "Fácil"
    private var targetScore = 30
    private var colorsPerRound = 3

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        when (difficulty) {
            "Fácil" -> {
                targetScore = 30
                colorsPerRound = 3
                _availableColors.value = baseColors
            }
            "Medio" -> {
                targetScore = 40
                colorsPerRound = 5
                _availableColors.value = baseColors + extraColors
            }
            "Difícil" -> {
                targetScore = 60
                colorsPerRound = 7
                _availableColors.value = baseColors + extraColors
            }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _lives.value = 3
        when (currentDifficulty) {
            "Fácil" -> colorsPerRound = 3
            "Medio" -> colorsPerRound = 5
            "Difícil" -> colorsPerRound = 7
        }
        _phase.value = GamePhase.INTRO
    }

    fun startRound() {
        if (_lives.value <= 0) {
            _phase.value = GamePhase.GAMEOVER_LOSE
            return
        }
        _userSelectedColors.value = emptyList()
        val shuffled = _availableColors.value.shuffled().take(colorsPerRound)
        _colorsToMemorize.value = shuffled
        _phase.value = GamePhase.MEMORIZE
    }

    fun readyToSelect() {
        _phase.value = GamePhase.SELECT
    }

    fun selectColor(color: Color) {
        if (_phase.value != GamePhase.SELECT) return
        val currentSelected = _userSelectedColors.value.toMutableList()

        if (currentSelected.contains(color)) {
            currentSelected.remove(color)
        } else if (currentSelected.size < _colorsToMemorize.value.size) {
            currentSelected.add(color)
        }
        _userSelectedColors.value = currentSelected
    }

    fun verifySelection() {
        if (_phase.value != GamePhase.SELECT || _userSelectedColors.value.size != _colorsToMemorize.value.size) return

        val correctColors = _colorsToMemorize.value
        val userColors = _userSelectedColors.value

        var aciertos = 0
        for (i in correctColors.indices) {
            if (userColors[i] == correctColors[i]) aciertos++
        }

        val errores = correctColors.size - aciertos

        _score.value += aciertos
        _lives.value -= errores

        if (_score.value >= targetScore) {
            _phase.value = GamePhase.GAMEOVER_WIN
            saveScoreToFirebase()
        } else if (_lives.value <= 0) {
            _phase.value = GamePhase.GAMEOVER_LOSE
            saveScoreToFirebase()
        } else {
            colorsPerRound = minOf(_availableColors.value.size - 1, colorsPerRound + 1)
            startRound()
        }
    }

    fun playAgain() {
        resetStats()
        startRound()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Icónica",
            "gameName" to "Color",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}