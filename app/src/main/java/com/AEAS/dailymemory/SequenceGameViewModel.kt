package com.AEAS.dailymemory

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SequencePhase { INTRO, MEMORIZE, RECALL, GAMEOVER_WIN, GAMEOVER_LOSE }

class SequenceGameViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(SequencePhase.INTRO)
    val phase: StateFlow<SequencePhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _correctSequence = MutableStateFlow<List<Int>>(emptyList())
    val correctSequence: StateFlow<List<Int>> = _correctSequence.asStateFlow()

    private val _poolImages = MutableStateFlow<List<Int>>(emptyList())
    val poolImages: StateFlow<List<Int>> = _poolImages.asStateFlow()

    private val _poolHidden = MutableStateFlow<List<Boolean>>(emptyList())
    val poolHidden: StateFlow<List<Boolean>> = _poolHidden.asStateFlow()

    private val _userSlots = MutableStateFlow<List<Int?>>(emptyList())
    val userSlots: StateFlow<List<Int?>> = _userSlots.asStateFlow()

    var currentDifficulty = "Fácil"
    private var imagesCount = 4
    private val maxLevels = 10

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        imagesCount = when (difficulty) {
            "Fácil" -> 4
            "Medio" -> 6
            "Difícil" -> 8
            else -> 4
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _lives.value = 3
        _currentLevel.value = 1
        _phase.value = SequencePhase.INTRO
    }

    fun startRound() {
        if (_lives.value <= 0) {
            _phase.value = SequencePhase.GAMEOVER_LOSE
            return
        }

        val allImages = listOf(1, 2, 3, 4, 5, 6, 7, 8)
        val selectedImages = allImages.shuffled().take(imagesCount)

        _correctSequence.value = selectedImages
        _poolImages.value = selectedImages.shuffled()
        _poolHidden.value = List(imagesCount) { false }
        _userSlots.value = List(imagesCount) { null }

        _phase.value = SequencePhase.MEMORIZE
    }

    fun readyToRecall() {
        _phase.value = SequencePhase.RECALL
    }

    fun selectFromPool(poolIndex: Int, imageId: Int) {
        if (_phase.value != SequencePhase.RECALL || _poolHidden.value[poolIndex]) return

        val slots = _userSlots.value.toMutableList()
        val emptyIndex = slots.indexOfFirst { it == null }

        if (emptyIndex != -1) {
            slots[emptyIndex] = imageId
            _userSlots.value = slots

            val hidden = _poolHidden.value.toMutableList()
            hidden[poolIndex] = true
            _poolHidden.value = hidden
        }
    }

    fun deselectFromSlot(slotIndex: Int) {
        if (_phase.value != SequencePhase.RECALL) return

        val slots = _userSlots.value.toMutableList()
        val imageId = slots[slotIndex] ?: return

        slots[slotIndex] = null
        _userSlots.value = slots

        val poolIndex = _poolImages.value.indexOf(imageId)
        if (poolIndex != -1) {
            val hidden = _poolHidden.value.toMutableList()
            hidden[poolIndex] = false
            _poolHidden.value = hidden
        }
    }

    fun verifySelection() {
        if (_phase.value != SequencePhase.RECALL) return
        if (_userSlots.value.contains(null)) return

        var aciertos = 0
        val correct = _correctSequence.value
        val user = _userSlots.value

        for (i in correct.indices) {
            if (correct[i] == user[i]) aciertos++
        }

        val errores = correct.size - aciertos
        _score.value += aciertos
        _lives.value -= errores

        if (_lives.value <= 0) {
            _phase.value = SequencePhase.GAMEOVER_LOSE
            saveScoreToFirebase()
        } else if (_currentLevel.value >= maxLevels) {
            _phase.value = SequencePhase.GAMEOVER_WIN
            saveScoreToFirebase()
        } else {
            _currentLevel.value++
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
            "gameName" to "Secuencia",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}