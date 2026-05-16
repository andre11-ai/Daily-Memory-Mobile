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

enum class MemoramaPhase { INTRO, PLAYING, GAMEOVER_WIN, GAMEOVER_LOSE }

data class MemoramaCard(
    val id: Int,
    val pairId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

class MemoramaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(MemoramaPhase.INTRO)
    val phase: StateFlow<MemoramaPhase> = _phase.asStateFlow()

    private val _cards = MutableStateFlow<List<MemoramaCard>>(emptyList())
    val cards: StateFlow<List<MemoramaCard>> = _cards.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves: StateFlow<Int> = _moves.asStateFlow()

    private val _matches = MutableStateFlow(0)
    val matches: StateFlow<Int> = _matches.asStateFlow()

    private val _timeLeft = MutableStateFlow(120)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    var currentDifficulty = "Fácil"
    private var initialTime = 120
    private var timerJob: Job? = null

    private var firstSelectedCard: MemoramaCard? = null
    private var isBoardLocked = false

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        initialTime = when (difficulty) {
            "Fácil" -> 120
            "Medio" -> 90
            "Difícil" -> 60
            else -> 120
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _moves.value = 0
        _matches.value = 0
        _timeLeft.value = initialTime
        firstSelectedCard = null
        isBoardLocked = false

        val newCards = mutableListOf<MemoramaCard>()
        val pairs = listOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8).shuffled()

        pairs.forEachIndexed { index, pairId ->
            newCards.add(MemoramaCard(id = index, pairId = pairId))
        }
        _cards.value = newCards
        _phase.value = MemoramaPhase.INTRO

        timerJob?.cancel()
    }

    fun startGame() {
        _phase.value = MemoramaPhase.PLAYING
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && _phase.value == MemoramaPhase.PLAYING) {
                delay(1000)
                _timeLeft.value -= 1
            }
            if (_timeLeft.value <= 0 && _phase.value == MemoramaPhase.PLAYING) {
                endGame(win = false)
            }
        }
    }

    fun flipCard(cardIndex: Int) {
        if (isBoardLocked || _phase.value != MemoramaPhase.PLAYING) return

        val currentCards = _cards.value.toMutableList()
        val clickedCard = currentCards[cardIndex]

        if (clickedCard.isFlipped || clickedCard.isMatched) return

        currentCards[cardIndex] = clickedCard.copy(isFlipped = true)
        _cards.value = currentCards

        if (firstSelectedCard == null) {
            firstSelectedCard = currentCards[cardIndex]
        } else {
            _moves.value += 1
            val secondCard = currentCards[cardIndex]
            isBoardLocked = true

            if (firstSelectedCard!!.pairId == secondCard.pairId) {
                _matches.value += 1
                _score.value += 1

                val updatedCards = _cards.value.toMutableList()
                updatedCards[firstSelectedCard!!.id] = firstSelectedCard!!.copy(isFlipped = true, isMatched = true)
                updatedCards[secondCard.id] = secondCard.copy(isFlipped = true, isMatched = true)
                _cards.value = updatedCards

                firstSelectedCard = null
                isBoardLocked = false

                if (_matches.value == 8) {
                    endGame(win = true)
                }
            } else {
                viewModelScope.launch {
                    delay(800)
                    val resetCards = _cards.value.toMutableList()
                    resetCards[firstSelectedCard!!.id] = firstSelectedCard!!.copy(isFlipped = false)
                    resetCards[secondCard.id] = secondCard.copy(isFlipped = false)
                    _cards.value = resetCards

                    firstSelectedCard = null
                    isBoardLocked = false
                }
            }
        }
    }

    private fun endGame(win: Boolean) {
        timerJob?.cancel()
        _phase.value = if (win) MemoramaPhase.GAMEOVER_WIN else MemoramaPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        resetStats()
        startGame()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Icónica",
            "gameName" to "Memorama",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "moves" to _moves.value,
            "timeLeft" to _timeLeft.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}