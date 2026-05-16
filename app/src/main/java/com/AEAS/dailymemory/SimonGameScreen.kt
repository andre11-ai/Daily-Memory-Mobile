package com.AEAS.dailymemory

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SimonGameScreen(
    difficulty: String,
    onNavigateBack: () -> Unit,
    viewModel: SimonGameViewModel = viewModel()
) {
    LaunchedEffect(difficulty) {
        viewModel.initGame(difficulty)
    }

    val phase by viewModel.phase.collectAsState()
    val score by viewModel.score.collectAsState()
    val sequence by viewModel.sequence.collectAsState()
    val activeButton by viewModel.activeButton.collectAsState()

    val numButtons = viewModel.numButtons
    val colors = when (numButtons) {
        4 -> listOf(Color(0xFF28CF5F), Color(0xFFF54F4F), Color(0xFF2FB7FF), Color(0xFFFFD43B))
        5 -> viewModel.colorsMedium
        else -> viewModel.colorsHard
    }

    val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    var soundEnabled by remember { mutableStateOf(viewModel.isSoundEnabled) }
    var strictMode by remember { mutableStateOf(false) }

    LaunchedEffect(activeButton) {
        if (activeButton != null && viewModel.isSoundEnabled) {
            val tone = ToneGenerator.TONE_DTMF_1 + activeButton!!
            toneGen.startTone(tone, 200)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Memoria Ecoica", color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Simon $difficulty", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222222))
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(horizontal = 24.dp, vertical = 8.dp)) {
                        Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF222222))
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    val boardSize = minOf(maxWidth - 32.dp, maxHeight - 100.dp, 420.dp) // Tamaño máximo
                    val strokeWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { 16.dp.toPx() }

                    val startAngleOffset = -180f

                    Box(
                        modifier = Modifier.size(boardSize)
                            .shadow(20.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF181D25))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    if (phase != SimonPhase.WAITING_PLAYER) return@detectTapGestures

                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                                    val outerRadius = size.width / 2f
                                    val innerRadius = outerRadius * 0.45f

                                    if (distance in innerRadius..outerRadius) {
                                        var angle = (atan2(dy.toDouble(), dx.toDouble()) * 180 / PI).toFloat()
                                        angle -= startAngleOffset
                                        while (angle < 0) angle += 360f

                                        val sweepAngle = 360f / numButtons
                                        val index = (angle / sweepAngle).toInt() % numButtons

                                        viewModel.onPlayerInput(index)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            val sweepAngle = 360f / numButtons

                            for (i in 0 until numButtons) {
                                val isActive = (activeButton == i)
                                drawArc(
                                    color = if (isActive) Color.White else colors[i],
                                    startAngle = startAngleOffset + (i * sweepAngle),
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    alpha = if (isActive) 0.8f else 1f
                                )
                            }

                            val center = Offset(size.width / 2f, size.height / 2f)
                            for (i in 0 until numButtons) {
                                val angle = startAngleOffset + (i * sweepAngle)
                                val rad = angle * PI / 180
                                val endX = center.x + cos(rad).toFloat() * size.width / 2
                                val endY = center.y + sin(rad).toFloat() * size.height / 2
                                drawLine(
                                    color = Color(0xFF181D25),
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(0.48f).clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFF222934), Color(0xFF191D21))))
                                .border(12.dp, Color(0xFF181D25), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SIMON", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = 2.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                val displayCount = if (phase == SimonPhase.GAMEOVER_LOSE) "!!" else if (sequence.isEmpty()) "--" else sequence.size.toString().padStart(2, '0')

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.background(Color(0xFF0F1318), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(displayCount, color = Color(0xFF2FB7FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Box(modifier = Modifier.background(Color(0xFF232D38), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.startGame() }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text("Start", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Box(modifier = Modifier.background(if (strictMode) MintPrimary else Color(0xFF232D38), RoundedCornerShape(6.dp))
                                        .clickable { strictMode = !strictMode }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text("Strict", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.background(if (soundEnabled) Color(0xFF232D38) else Color(0xFFF54F4F), RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.toggleSound()
                                        soundEnabled = viewModel.isSoundEnabled
                                    }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    Text(if (soundEnabled) "Sonido" else "Mute", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (phase == SimonPhase.INTRO || phase == SimonPhase.GAMEOVER_WIN || phase == SimonPhase.GAMEOVER_LOSE) {
            SimonHelpModal(
                phase = phase, difficulty = difficulty, score = score, target = viewModel.targetRound,
                onActionClick = { viewModel.startGame() },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun SimonHelpModal(phase: SimonPhase, difficulty: String, score: Int, target: Int, onActionClick: () -> Unit, onNavigateBack: () -> Unit) {
    val themeColor = when (phase) { SimonPhase.GAMEOVER_WIN -> Color(0xFFFFD700); SimonPhase.GAMEOVER_LOSE -> Color(0xFFFF4F68); else -> MintPrimary }
    val eyebrowText = when (phase) { SimonPhase.GAMEOVER_WIN -> "¡VICTORIA!"; SimonPhase.GAMEOVER_LOSE -> "FALLASTE"; else -> "MEMORIA ECOICA" }
    val titleText = when (phase) { SimonPhase.GAMEOVER_WIN -> "¡Lo lograste!"; SimonPhase.GAMEOVER_LOSE -> "Secuencia incorrecta"; else -> "Simon $difficulty" }

    val msgText = when (phase) {
        SimonPhase.GAMEOVER_WIN -> "Tu memoria secuencial es excelente."
        SimonPhase.GAMEOVER_LOSE -> "Te equivocaste de botón. ¡Inténtalo otra vez!"
        else -> "Repite la secuencia de luces y sonidos.\nMeta: $target rondas."
    }
    val btnText = when (phase) { SimonPhase.GAMEOVER_WIN -> "Jugar de nuevo"; SimonPhase.GAMEOVER_LOSE -> "Reintentar"; else -> "¡Empezar!" }

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

                    if (phase != SimonPhase.INTRO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntaje final: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (phase != SimonPhase.INTRO) {
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