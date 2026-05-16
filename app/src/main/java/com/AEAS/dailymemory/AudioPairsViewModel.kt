package com.AEAS.dailymemory

import android.content.Context
import android.speech.tts.TextToSpeech
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
import java.util.*

enum class AudioPairsPhase { INTRO, PLAYING, GAMEOVER_WIN, GAMEOVER_LOSE }

data class AudioCard(
    val id: Int,
    val word: String,
    var isSelected: Boolean = false,
    var isMatched: Boolean = false
)

class AudioPairsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var tts: TextToSpeech? = null

    private val _phase = MutableStateFlow(AudioPairsPhase.INTRO)
    val phase: StateFlow<AudioPairsPhase> = _phase.asStateFlow()

    private val _cards = MutableStateFlow<List<AudioCard>>(emptyList())
    val cards: StateFlow<List<AudioCard>> = _cards.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("")
    val feedbackMessage: StateFlow<String> = _feedbackMessage.asStateFlow()

    var currentDifficulty = "Fácil"
    private var initialTime = 60
    private var totalPairs = 6
    private var timerJob: Job? = null

    private var firstSelectedCard: AudioCard? = null
    private var isBoardLocked = false

    private val easyWords = listOf("Gato", "Perro", "Pato", "Humano", "Mesa", "Silla")
    private val mediumWords = listOf("Campana", "Tambor", "Rana", "Zapato", "Perro", "Pato", "Humano", "León", "Acordeón")
    private val hardWords = listOf("Palma", "Golpe", "Chasquido", "Viento", "Lluvia", "Guitarra", "Humano", "León", "Peluche")

    fun initGame(context: Context, difficulty: String) {
        currentDifficulty = difficulty

        when (difficulty) {
            "Fácil" -> { initialTime = 60; totalPairs = 6 }
            "Medio" -> { initialTime = 50; totalPairs = 9 }
            "Difícil" -> { initialTime = 40; totalPairs = 9 }
        }

        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("es", "ES")
                }
            }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _timeLeft.value = initialTime
        _feedbackMessage.value = ""
        firstSelectedCard = null
        isBoardLocked = false
        _phase.value = AudioPairsPhase.INTRO

        val wordsPool = when (currentDifficulty) {
            "Fácil" -> easyWords
            "Medio" -> mediumWords
            else -> hardWords
        }

        val deck = (wordsPool + wordsPool).shuffled()

        val newCards = deck.mapIndexed { index, word ->
            AudioCard(id = index, word = word)
        }
        _cards.value = newCards
        timerJob?.cancel()
    }

    fun startGame() {
        _phase.value = AudioPairsPhase.PLAYING
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && _phase.value == AudioPairsPhase.PLAYING) {
                delay(1000)
                _timeLeft.value -= 1
            }
            if (_timeLeft.value <= 0 && _phase.value == AudioPairsPhase.PLAYING) {
                endGame(win = false)
            }
        }
    }

    fun selectCard(cardIndex: Int) {
        if (isBoardLocked || _phase.value != AudioPairsPhase.PLAYING) return

        val currentCards = _cards.value.toMutableList()
        val clickedCard = currentCards[cardIndex]

        if (clickedCard.isSelected || clickedCard.isMatched) return

        tts?.speak(clickedCard.word, TextToSpeech.QUEUE_FLUSH, null, null)

        currentCards[cardIndex] = clickedCard.copy(isSelected = true)
        _cards.value = currentCards

        if (firstSelectedCard == null) {
            firstSelectedCard = currentCards[cardIndex]
            _feedbackMessage.value = "Escuchando..."
        } else {
            val secondCard = currentCards[cardIndex]
            isBoardLocked = true

            if (firstSelectedCard!!.word == secondCard.word) {
                _score.value += 1
                _feedbackMessage.value = "¡Pareja encontrada!"

                val updatedCards = _cards.value.toMutableList()
                updatedCards[firstSelectedCard!!.id] = firstSelectedCard!!.copy(isMatched = true, isSelected = false)
                updatedCards[secondCard.id] = secondCard.copy(isMatched = true, isSelected = false)
                _cards.value = updatedCards

                firstSelectedCard = null
                isBoardLocked = false

                if (_score.value == totalPairs) {
                    endGame(win = true)
                }
            } else {
                _feedbackMessage.value = "Intenta de nuevo."
                viewModelScope.launch {
                    delay(900)
                    val resetCards = _cards.value.toMutableList()
                    resetCards[firstSelectedCard!!.id] = firstSelectedCard!!.copy(isSelected = false)
                    resetCards[secondCard.id] = secondCard.copy(isSelected = false)
                    _cards.value = resetCards

                    _feedbackMessage.value = ""
                    firstSelectedCard = null
                    isBoardLocked = false
                }
            }
        }
    }

    private fun endGame(win: Boolean) {
        timerJob?.cancel()
        _phase.value = if (win) AudioPairsPhase.GAMEOVER_WIN else AudioPairsPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        resetStats()
        startGame()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Ecoica",
            "gameName" to "Sonido Pareja",
            "difficulty" to currentDifficulty,
            "score" to _score.value,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}