package com.AEAS.dailymemory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypingSpeedScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: TypingSpeedViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val words by viewModel.words.collectAsState()
    val currentInput by viewModel.currentInput.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val wpm by viewModel.wpm.collectAsState()

    val focusRequester = remember { FocusRequester() }

    // Pedir foco automático al empezar a jugar para que se abra el teclado
    LaunchedEffect(phase) {
        if (phase == TypingPhase.PLAYING) {
            try { focusRequester.requestFocus() } catch (e: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- NAVBAR ---
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Memoria Muscular", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- HEADER DEL JUEGO ---
                Text(
                    text = "Velocímetro ($difficulty)",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )

                // --- CONTENEDOR CENTRAL ---
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier.widthIn(max = 800.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Caja de palabras a escribir
                        Card(
                            modifier = Modifier.fillMaxWidth().height(250.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, MintPrimary.copy(alpha = 0.3f))
                        ) {
                            val scrollState = rememberScrollState()

                            if (phase == TypingPhase.INTRO) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("⌛", fontSize = 40.sp)
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    words.forEach { word ->
                                        WordItem(word)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Controles de escritura (Input, Tiempo, Restart)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // TextField (El teclado)
                            OutlinedTextField(
                                value = currentInput,
                                onValueChange = { viewModel.onInputChanged(it) },
                                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                                enabled = phase == TypingPhase.PLAYING,
                                placeholder = { Text("Escribe aquí...", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Text),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MintPrimary,
                                    unfocusedBorderColor = Color(0xFFB3E2DE),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                            )

                            // Reloj
                            Box(
                                modifier = Modifier.height(56.dp).background(MintPrimary, RoundedCornerShape(16.dp)).padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("0:${timeLeft.toString().padStart(2, '0')}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }

                            // Reiniciar
                            Box(
                                modifier = Modifier.size(56.dp).background(AccentBlue, RoundedCornerShape(16.dp)).clickable { viewModel.initGame(difficulty) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- MODALES ---
        if (phase != TypingPhase.PLAYING) {
            TypingModal(
                phase = phase, difficulty = difficulty, wpm = wpm, targetWpm = viewModel.targetWpm,
                onActionClick = { if (phase == TypingPhase.INTRO) viewModel.startGame() else viewModel.playAgain() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun WordItem(word: TypedWord) {
    // Estilos según el estado (Igual que en tu CSS)
    val bgColor = when (word.state) {
        WordState.CURRENT_VALID, WordState.CURRENT_INVALID -> Color(0xFFE0FFFA)
        else -> Color.Transparent
    }

    val textColor = when (word.state) {
        WordState.CURRENT_VALID -> MintPrimary
        WordState.CURRENT_INVALID, WordState.INCORRECT -> Color(0xFFE74C3C) // Rojo
        WordState.CORRECT -> Color(0xFF2ECC71) // Verde
        else -> Color(0xFF555555) // Gris normal
    }

    val textDeco = if (word.state == WordState.INCORRECT) TextDecoration.LineThrough else TextDecoration.None

    val borderColor = if (word.state == WordState.CURRENT_VALID || word.state == WordState.CURRENT_INVALID) MintPrimary else Color.Transparent

    Text(
        text = word.text,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        textDecoration = textDeco,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun TypingModal(phase: TypingPhase, difficulty: String, wpm: Int, targetWpm: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { TypingPhase.GAMEOVER_WIN -> Color(0xFFFFD700); TypingPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }

    val eyebrowText = when (phase) {
        TypingPhase.GAMEOVER_WIN -> "¡EXCELENTE!"
        TypingPhase.GAMEOVER_LOSE -> "SIGUE PRACTICANDO"
        else -> "VELOCÍMETRO"
    }

    val titleText = when (phase) {
        TypingPhase.GAMEOVER_WIN -> "¡Meta Superada!"
        TypingPhase.GAMEOVER_LOSE -> "Buen intento"
        else -> "Nivel $difficulty"
    }

    val msgText = when (phase) {
        TypingPhase.GAMEOVER_WIN -> "Tus dedos vuelan sobre el teclado."
        TypingPhase.GAMEOVER_LOSE -> "Intenta escribir con más precisión y velocidad."
        else -> "Escribe las palabras lo más rápido que puedas.\nMeta: $targetWpm Palabras por Minuto (WPM)."
    }

    val btnText = when (phase) { TypingPhase.GAMEOVER_WIN -> "Jugar de nuevo"; TypingPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF001E28).copy(alpha = 0.6f)).padding(24.dp).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingMascot(size = 180.dp)
            Canvas(modifier = Modifier.size(width = 16.dp, height = 24.dp)) {
                val path = Path().apply { moveTo(size.width, 0f); lineTo(0f, size.height / 2f); lineTo(size.width, size.height); close() }
                drawPath(path, themeColor)
                val innerPath = Path().apply { moveTo(size.width, 2f); lineTo(3f, size.height / 2f); lineTo(size.width, size.height - 2f); close() }
                drawPath(innerPath, Color.White)
            }
            Card(
                shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White),
                border = BorderStroke(2.dp, themeColor), modifier = Modifier.widthIn(min = 350.dp, max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(eyebrowText, color = themeColor, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(titleText, color = BlueDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(msgText, color = Color.Gray, fontSize = 16.sp, lineHeight = 24.sp)

                    if (phase != TypingPhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Velocidad final: $wpm WPM", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != TypingPhase.INTRO) {
                            Text("Volver al Menú", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.clickable { onNavigateBack() }.padding(end = 16.dp))
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(MintPrimary, AccentBlue)))
                                .clickable { onActionClick() }.padding(horizontal = 32.dp, vertical = 12.dp)
                        ) { Text(btnText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) }
                    }
                }
            }
        }
    }
}