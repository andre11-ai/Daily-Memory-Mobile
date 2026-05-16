package com.AEAS.dailymemory

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

enum class WordPhase { INTRO, LISTENING, PLAYING, ROUND_RESULT, GAMEOVER_WIN, GAMEOVER_LOSE }

class WordRepeatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var tts: TextToSpeech? = null

    private val _phase = MutableStateFlow(WordPhase.INTRO)
    val phase: StateFlow<WordPhase> = _phase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _round = MutableStateFlow(1)
    val round: StateFlow<Int> = _round.asStateFlow()

    private val _sequence = MutableStateFlow<List<String>>(emptyList())

    private val _availableWords = MutableStateFlow<List<String>>(emptyList())
    val availableWords: StateFlow<List<String>> = _availableWords.asStateFlow()

    private val _userSelection = MutableStateFlow<List<String>>(emptyList())
    val userSelection: StateFlow<List<String>> = _userSelection.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("")
    val feedbackMessage: StateFlow<String> = _feedbackMessage.asStateFlow()

    var currentDifficulty = "Fácil"
    private var targetRounds = 10
    private var isCorrectRound = false

    private val easyWords = listOf("Gato", "Perro", "Pato")
    private val mediumWords = listOf("Campana", "Tambor", "Rana", "Zapato")
    private val hardWords = listOf("Palma", "Golpe", "Chasquido", "Viento", "Lluvia")

    fun initGame(context: Context, difficulty: String) {
        currentDifficulty = difficulty

        when (difficulty) {
            "Fácil" -> { targetRounds = 8; _availableWords.value = easyWords }
            "Medio" -> { targetRounds = 10; _availableWords.value = mediumWords }
            "Difícil" -> { targetRounds = 12; _availableWords.value = hardWords }
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }
        resetStats()
    }

    private fun resetStats() {
        _score.value = 0
        _round.value = 1
        _phase.value = WordPhase.INTRO
        _userSelection.value = emptyList()
        _sequence.value = emptyList()
    }

    fun startNextRound() {
        _phase.value = WordPhase.LISTENING
        _userSelection.value = emptyList()
        _feedbackMessage.value = "Escucha atentamente..."

        val itemsToAdd = if (currentDifficulty == "Fácil") 1 else 2
        val currentSeq = mutableListOf<String>()
        val wordsPool = _availableWords.value

        for (i in 0 until (_round.value + itemsToAdd)) {
            currentSeq.add(wordsPool.random())
        }
        _sequence.value = currentSeq

        viewModelScope.launch {
            delay(1000)
            for (word in currentSeq) {
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                delay(if (currentDifficulty == "Difícil") 1200L else 1500L)
            }
            _feedbackMessage.value = "¡Tu turno! Selecciona el orden correcto:"
            _phase.value = WordPhase.PLAYING
        }
    }

    fun selectWord(word: String) {
        if (_phase.value != WordPhase.PLAYING) return

        val currentSelection = _userSelection.value.toMutableList()
        currentSelection.add(word)
        _userSelection.value = currentSelection

        if (currentSelection.size == _sequence.value.size) {
            verifySelection()
        }
    }

    private fun verifySelection() {
        isCorrectRound = _userSelection.value == _sequence.value

        if (isCorrectRound) {
            _score.value += 1
            _feedbackMessage.value = "¡Correcto! Preparando siguiente ronda..."
            if (_score.value >= targetRounds) {
                endGame(win = true)
            } else {
                _phase.value = WordPhase.ROUND_RESULT
                viewModelScope.launch {
                    delay(1500)
                    _round.value += 1
                    startNextRound()
                }
            }
        } else {
            _feedbackMessage.value = "Incorrecto."
            _phase.value = WordPhase.ROUND_RESULT
            viewModelScope.launch {
                delay(1500)
                endGame(win = false)
            }
        }
    }

    private fun endGame(win: Boolean) {
        _phase.value = if (win) WordPhase.GAMEOVER_WIN else WordPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        resetStats()
        startNextRound()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Ecoica",
            "gameName" to "Repetir Palabra",
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