package com.AEAS.dailymemory

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

val LevelBtnColor = Color(0xFF3E4EFF)
val LevelBtnHover = Color(0xFF232B8D)
val TextGrey = Color(0xFF555555)


@Composable
fun TiposMemoriaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToIconic: () -> Unit,
    onNavigateToMuscular: () -> Unit,
    onNavigateToEchoic: () -> Unit
) {
    MemoryBaseScreen(
        title = "Daily Memory",
        subtitle = "Tipos de Memoria",
        helpTitle = "¿Qué son?",
        helpSubtitle = "Selecciona una categoría para ver sus niveles:",
        helpBullets = listOf(
            "Icónica:" to "Estímulos visuales e imágenes.",
            "Muscular:" to "Coordinación y reflejos.",
            "Ecoica:" to "Sonidos y palabras."
        ),
        onNavigateBack = onNavigateBack
    ) {
        TwoPlusOneGrid(
            item1 = { mod -> CategoryCard("Memoria Icónica", "Ejercicios para mejorar tu capacidad de recordar imágenes y estímulos visuales.", Icons.Default.Image, mod, onNavigateToIconic) },
            item2 = { mod -> CategoryCard("Memoria Muscular", "Desafía tu mente y cuerpo con ejercicios y juegos que estimulan la memoria muscular.", Icons.Default.FitnessCenter, mod, onNavigateToMuscular) },
            item3 = { mod -> CategoryCard("Memoria Ecoica", "Entrena tu memoria para reconocer y recordar patrones de sonidos y palabras.", Icons.Default.Mic, mod, onNavigateToEchoic) }
        )
    }
}

@Composable
fun IconicaScreen(onNavigateBack: () -> Unit, onNavigateToGame: (String) -> Unit) {
    MemoryBaseScreen(
        title = "Memoria Icónica",
        subtitle = "Escoge un juego",
        helpTitle = "¿Cómo jugar?",
        helpSubtitle = "Entrena tu memoria visual con Color, Memorama y Secuencias.",
        helpBullets = listOf(
            "Fácil:" to "Pocos elementos y más tiempo para aprender.",
            "Medio:" to "Velocidad estándar y más objetos para recordar.",
            "Difícil:" to "Muy rápido, secuencias largas y menos tiempo."
        ),
        onNavigateBack = onNavigateBack
    ) {
        TwoPlusOneGrid(
            item1 = { mod -> GameCard("Color", "Juego de colores y memoria visual.", Icons.Default.Palette, mod) { diff -> onNavigateToGame("Color_$diff") } },
            item2 = { mod -> GameCard("Memorama", "Encuentra pares de imágenes iguales.", Icons.Default.GridView, mod) { diff -> onNavigateToGame("Memorama_$diff") } },
            item3 = { mod -> GameCard("Secuencia de Imágenes", "Entrena tu memoria visual con secuencias de imágenes.", Icons.Default.BurstMode, mod) { diff -> onNavigateToGame("Secuencia_$diff") } }
        )
    }
}

@Composable
fun MuscularScreen(onNavigateBack: () -> Unit, onNavigateToGame: (String) -> Unit) {
    MemoryBaseScreen(
        title = "Memoria Muscular",
        subtitle = "Escoge un juego",
        helpTitle = "¿Cómo funciona?",
        helpSubtitle = "Mejora tus reflejos y coordinación motora.",
        helpBullets = listOf(
            "Fácil:" to "Velocidad lenta, palabras cortas y más tiempo.",
            "Medio:" to "Ritmo constante y palabras de longitud media.",
            "Difícil:" to "Velocidad extrema, palabras largas y poco tiempo."
        ),
        onNavigateBack = onNavigateBack
    ) {
        TwoPlusOneGrid(
            item1 = { mod -> GameCard("Scary Witch Typing", "Teclado, agilidad y memoria muscular.", Icons.Default.Keyboard, mod) { diff -> onNavigateToGame("Scary_$diff") } },
            item2 = { mod -> GameCard("Velocímetro", "Velocidad y precisión con las manos.", Icons.Default.Speed, mod) { diff -> onNavigateToGame("Velocimetro_$diff") } },
            item3 = { mod -> GameCard("Lluvia de letras", "Mejora la velocidad y precisión al escribir.", Icons.Default.FontDownload, mod) { diff -> onNavigateToGame("Lluvia_$diff") } }
        )
    }
}

@Composable
fun EcoicaScreen(onNavigateBack: () -> Unit, onNavigateToGame: (String) -> Unit) {
    MemoryBaseScreen(
        title = "Memoria Ecoica",
        subtitle = "Escoge un juego",
        helpTitle = "¿Qué escucharás?",
        helpSubtitle = "Ejercita tu oído con Simón, Repetir Palabra y Sonido Pareja.",
        helpBullets = listOf(
            "Fácil:" to "Secuencias cortas, sonidos lentos y claros.",
            "Medio:" to "Mayor velocidad y secuencias más largas.",
            "Difícil:" to "Sonidos complejos, muy rápido y secuencias extensas."
        ),
        onNavigateBack = onNavigateBack
    ) {
        TwoPlusOneGrid(
            item1 = { mod -> GameCard("Simon", "Escucha y repite secuencias de sonidos.", Icons.Default.GraphicEq, mod) { diff -> onNavigateToGame("Simon_$diff") } },
            item2 = { mod -> GameCard("Repetir La Palabra", "Escucha y repite palabras para entrenar tu memoria auditiva.", Icons.Default.RecordVoiceOver, mod) { diff -> onNavigateToGame("Palabra_$diff") } },
            item3 = { mod -> GameCard("Encuentra el Sonido Pareja", "Encuentra pares de sonidos iguales.", Icons.Default.Headphones, mod) { diff -> onNavigateToGame("Sonido_$diff") } }
        )
    }
}

@Composable
fun TwoPlusOneGrid(
    item1: @Composable (Modifier) -> Unit,
    item2: @Composable (Modifier) -> Unit,
    item3: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isCompact = maxWidth < 600.dp
        if (isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                item1(Modifier.fillMaxWidth())
                item2(Modifier.fillMaxWidth())
                item3(Modifier.fillMaxWidth())
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item1(Modifier.weight(1f).fillMaxHeight())
                    item2(Modifier.weight(1f).fillMaxHeight())
                }
                Spacer(modifier = Modifier.height(16.dp))
                item3(Modifier.fillMaxWidth(0.65f))
            }
        }
    }
}

@Composable
fun MemoryBaseScreen(
    title: String, subtitle: String, helpTitle: String, helpSubtitle: String, helpBullets: List<Pair<String, String>>,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var showHelpModal by remember { mutableStateOf(false) }
    val blurEffect by animateDpAsState(targetValue = if (showHelpModal) 16.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground(modifier = Modifier.blur(blurEffect)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().shadow(4.dp).background(Color(0xFFF8F9FA).copy(alpha = 0.95f))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, color = MintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (subtitle.isNotEmpty()) {
                        Text(subtitle, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = BlueDark)
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isTablet = maxWidth > 800.dp

                        if (isTablet) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(0.65f)) { content() }
                                Box(modifier = Modifier.weight(0.35f), contentAlignment = Alignment.Center) {
                                    FloatingMascot(size = 280.dp)
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                content()
                                Spacer(modifier = Modifier.height(32.dp))
                                FloatingMascot(size = 220.dp)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showHelpModal = true },
            containerColor = AccentBlue, contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), shape = CircleShape
        ) {
            Icon(Icons.Default.QuestionMark, contentDescription = "Ayuda")
        }

        if (showHelpModal) {
            MemoryHelpModal(title.uppercase(), helpTitle, helpSubtitle, helpBullets, onClose = { showHelpModal = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(title: String, desc: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(56.dp).background(ChatBgLight, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 14.sp, color = TextGrey, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.background(LevelBtnColor, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Ver niveles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GameCard(title: String, desc: String, icon: ImageVector, modifier: Modifier = Modifier, onDifficultyClick: (String) -> Unit) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(56.dp).background(ChatBgLight, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MintPrimary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlueDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 14.sp, color = TextGrey, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Botones de Dificultad estilo Web
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DifficultyButton("Fácil") { onDifficultyClick("Fácil") }
                    DifficultyButton("Medio") { onDifficultyClick("Medio") }
                    DifficultyButton("Difícil") { onDifficultyClick("Difícil") }
                }
            }
        }
    }
}

@Composable
fun DifficultyButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.background(LevelBtnColor, RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun MemoryHelpModal(header: String, title: String, subtitle: String, bullets: List<Pair<String, String>>, onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF001E28).copy(alpha = 0.5f)).padding(24.dp).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloatingMascot(size = 180.dp)
            Canvas(modifier = Modifier.size(width = 16.dp, height = 24.dp)) {
                val path = Path().apply { moveTo(size.width, 0f); lineTo(0f, size.height / 2f); lineTo(size.width, size.height); close() }
                drawPath(path, MintPrimary)
                val innerPath = Path().apply { moveTo(size.width, 2f); lineTo(3f, size.height / 2f); lineTo(size.width, size.height - 2f); close() }
                drawPath(innerPath, Color.White)
            }
            Card(
                shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White),
                border = BorderStroke(2.dp, MintPrimary), modifier = Modifier.widthIn(min = 350.dp, max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(header, color = MintPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, color = BlueDark, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(subtitle, color = Color.Gray, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (bullets.any { it.first.contains("Fácil") }) {
                        Text("Niveles de dificultad:", fontWeight = FontWeight.Bold, color = BlueDark, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        bullets.forEach { (level, desc) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier.background(LevelBtnColor, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(level.replace(":", ""), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(desc, color = Color.DarkGray, fontSize = 14.sp, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                            }
                        }
                    } else {
                        bullets.forEach { (bold, normal) ->
                            BulletText(bold, normal)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier.align(Alignment.End).clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(MintPrimary, AccentBlue)))
                            .clickable { onClose() }.padding(horizontal = 32.dp, vertical = 12.dp)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
