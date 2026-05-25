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

enum class TypingPhase { INTRO, PLAYING, GAMEOVER_WIN, GAMEOVER_LOSE }

enum class WordState { PENDING, CURRENT_VALID, CURRENT_INVALID, CORRECT, INCORRECT }

data class TypedWord(
    val text: String,
    var state: WordState = WordState.PENDING
)

class TypingSpeedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _phase = MutableStateFlow(TypingPhase.INTRO)
    val phase: StateFlow<TypingPhase> = _phase.asStateFlow()

    private val _words = MutableStateFlow<List<TypedWord>>(emptyList())
    val words: StateFlow<List<TypedWord>> = _words.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _wpm = MutableStateFlow(0)
    val wpm: StateFlow<Int> = _wpm.asStateFlow()

    var currentDifficulty = "Fácil"
    var targetWpm = 20
    private var timerJob: Job? = null

    private var currentWordIndex = 0
    private var totalTypedChars = 0
    private var incorrectWordsCount = 0

    // Listas de palabras originales de tu JS
    private val easyWords = listOf("sol", "pan", "luz", "mar", "sal", "ojo", "pie", "oso", "uva", "casa", "gato", "mesa", "agua", "tela", "pelo", "boca", "mano", "flor", "hoja", "caja", "bola", "dado", "nube", "luna", "aire", "rojo", "azul", "gris", "oro", "tren", "auto", "bici", "foto", "pato", "pez", "rana", "miel", "vaso", "taza", "pera", "lima", "uno", "dos", "tres", "ver", "ir", "dar", "ser", "rey", "voz", "rio", "lago", "foco", "cine", "mapa", "bebe", "mama", "papa", "libro", "perro", "papel", "goma", "silla", "cama", "fuego", "hielo", "playa", "radio", "reloj", "leche", "nieve", "amigos", "camino")
    private val mediumWords = listOf("eslogan", "llama", "importante", "nervios", "cabello", "señales", "piso", "temprano", "vaca", "zanahoria", "golpe", "bibliografía", "paleta", "defender", "pensar", "doblar", "decir", "marco", "huso", "despertar", "comercio", "amarillo", "billar", "ciudadano", "patinaje", "facilidad", "destello", "formación", "adelante", "gigante", "tripa", "equipo", "capas", "significado", "uña", "opción", "tierra", "lluvia", "sombra", "pronto", "lágrima", "tigre", "título", "salvaje", "animado", "ciego", "boceto", "caer", "hueso", "juguete", "paseo", "viento", "extraordinario", "transformación", "arquitectura", "conocimiento", "responsabilidad", "independencia", "comunicación", "investigación", "desarrollo", "imaginación", "creatividad", "sostenibilidad", "colaboración", "innovación", "tecnología", "educación", "cultura", "diversidad", "inclusión", "empatía", "resiliencia", "liderazgo", "motivación", "superación", "compromiso")
    private val hardWords = listOf("consigna", "flamear", "importante", "nervios", "pelo", "señales", "piso", "temprano", "vaca", "zanahoria", "acierto", "bibliografía", "chupetin", "defensa", "fink", "doblar", "dije", "marco", "huso", "despertar", "trocar", "amarillo", "billar", "ciudadano", "patinaje", "facilidad", "flash", "formacion", "adelante", "gigante", "tripa", "kit", "capas", "significado", "clavo", "opcion", "tierra", "lluvia", "sombra", "pronto", "sed", "desgarro", "tigre", "titulo", "salvaje", "animado", "ciego", "boceto", "el", "caer", "hueso", "juguete", "paseo", "viento")

    fun initGame(difficulty: String) {
        currentDifficulty = difficulty
        targetWpm = when (difficulty) {
            "Fácil" -> 20
            "Medio" -> 35
            "Difícil" -> 50
            else -> 20
        }
        resetGame()
    }

    private fun resetGame() {
        _phase.value = TypingPhase.INTRO
        _timeLeft.value = 60
        _wpm.value = 0
        _currentInput.value = ""
        currentWordIndex = 0
        totalTypedChars = 0
        incorrectWordsCount = 0
        timerJob?.cancel()

        val pool = when (currentDifficulty) {
            "Fácil" -> easyWords
            "Medio" -> mediumWords
            else -> hardWords
        }

        // Tomamos 80 palabras aleatorias para la partida
        val gameWords = pool.shuffled().take(80).map { TypedWord(it) }
        if (gameWords.isNotEmpty()) {
            gameWords[0].state = WordState.CURRENT_VALID
        }
        _words.value = gameWords
    }

    fun startGame() {
        _phase.value = TypingPhase.PLAYING
        _currentInput.value = ""

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && _phase.value == TypingPhase.PLAYING) {
                delay(1000)
                _timeLeft.value -= 1
            }
            if (_timeLeft.value <= 0) {
                finishGame()
            }
        }
    }

    fun onInputChanged(input: String) {
        if (_phase.value != TypingPhase.PLAYING) return

        // Si presiona Espacio, evaluamos la palabra
        if (input.endsWith(" ")) {
            val typedWord = input.trim()
            if (typedWord.isNotEmpty() || _currentInput.value.isNotEmpty()) {
                submitWord(typedWord)
            }
            return
        }

        _currentInput.value = input
        totalTypedChars++

        // Validar en tiempo real (Rojo o Verde)
        val currentList = _words.value.toMutableList()
        if (currentWordIndex < currentList.size) {
            val targetWord = currentList[currentWordIndex].text
            if (input.length > targetWord.length || !targetWord.startsWith(input)) {
                currentList[currentWordIndex] = currentList[currentWordIndex].copy(state = WordState.CURRENT_INVALID)
            } else {
                currentList[currentWordIndex] = currentList[currentWordIndex].copy(state = WordState.CURRENT_VALID)
            }
            _words.value = currentList
        }
    }

    private fun submitWord(typedWord: String) {
        val currentList = _words.value.toMutableList()
        if (currentWordIndex < currentList.size) {
            val targetWord = currentList[currentWordIndex].text

            if (typedWord == targetWord) {
                currentList[currentWordIndex] = currentList[currentWordIndex].copy(state = WordState.CORRECT)
            } else {
                currentList[currentWordIndex] = currentList[currentWordIndex].copy(state = WordState.INCORRECT)
                incorrectWordsCount++
            }

            currentWordIndex++

            // Marcar la siguiente como actual
            if (currentWordIndex < currentList.size) {
                currentList[currentWordIndex] = currentList[currentWordIndex].copy(state = WordState.CURRENT_VALID)
            } else {
                finishGame() // Se acabaron las palabras
            }

            _words.value = currentList
        }
        _currentInput.value = ""
    }

    private fun finishGame() {
        timerJob?.cancel()

        // Fórmula de WPM de tu JS: wpm = Math.floor((wordData.typed / 5) - wordData.incorrect)
        var calculatedWpm = (totalTypedChars / 5) - incorrectWordsCount
        if (calculatedWpm < 0) calculatedWpm = 0
        _wpm.value = calculatedWpm

        _phase.value = if (calculatedWpm >= targetWpm) TypingPhase.GAMEOVER_WIN else TypingPhase.GAMEOVER_LOSE
        saveScoreToFirebase()
    }

    fun playAgain() {
        resetGame()
        startGame()
    }

    private fun saveScoreToFirebase() {
        val currentUser = auth.currentUser ?: return
        val scoreRecord = hashMapOf(
            "gameType" to "Memoria Muscular",
            "gameName" to "Velocímetro",
            "difficulty" to currentDifficulty,
            "score" to _wpm.value, // Guardamos el WPM como score
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(currentUser.uid)
            .collection("scores")
            .add(scoreRecord)
    }
}