package com.AEAS.dailymemory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.BluePrimary
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground
import com.AEAS.dailymemory.R

@Composable
fun MenuScreen(
    onNavigateToMemoryTypes: () -> Unit,
    onNavigateToStory: () -> Unit,
    onNavigateToRelax: () -> Unit,
    onLogout: () -> Unit
) {

    val currentUser = FirebaseProviders.auth.currentUser
    val userDisplayName = currentUser?.displayName ?: "Usuario"

    val mintPrimary = Color(0xFF00C8A3)
    val lightMintBg = Color(0xFFEAFaf1)
    val footerBg = Color(0xFFF4F7FA)

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp) // Sombra
                    .background(Color(0xFFF8F9FA).copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Memory", color = mintPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { /* Ir a Perfil */ }) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Perfil", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            FirebaseProviders.auth.signOut()
                            onLogout()
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = BlueDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cerrar Sesión", color = BlueDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- CONTENIDO PRINCIPAL ---
            BoxWithConstraints(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                val isTablet = maxWidth > 600.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bienvenido, ",
                        fontSize = 32.sp,
                        color = BlueDark
                    )
                    Text(
                        text = userDisplayName,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueDark
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (isTablet) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(0.6f)) {
                                MenuGrid(mintPrimary, lightMintBg, onNavigateToRelax, onNavigateToMemoryTypes, onNavigateToStory)
                            }
                            Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.Center) {
                                FloatingMascot(size = 280.dp)
                            }
                        }
                    } else {
                        MenuGrid(mintPrimary, lightMintBg, onNavigateToRelax, onNavigateToMemoryTypes, onNavigateToStory)
                        Spacer(modifier = Modifier.height(32.dp))
                        FloatingMascot(size = 200.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(footerBg)
                    .padding(32.dp)
            ) {
                BoxWithConstraints {
                    val isTablet = maxWidth > 600.dp
                    if (isTablet) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Cerebro",
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.width(40.dp))
                            FooterText(mintPrimary)
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Cerebro",
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            FooterText(mintPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FooterText(mintPrimary: Color) {
    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
        Text(
            text = "Propósito",
            color = mintPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Daily Memory es una plataforma de estimulación cognitiva centrada en la memoria. Combina juegos diseñados para trabajar distintos tipos de memoria. Su objetivo es brindar una experiencia accesible y medible.",
            color = BlueDark,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun MenuGrid(
    mintPrimary: Color,
    lightMintBg: Color,
    onNavigateToRelax: () -> Unit,
    onNavigateToMemoryTypes: () -> Unit,
    onNavigateToStory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuGameCard(
                modifier = Modifier.weight(1f),
                title = "Juegos para Desestresar",
                icon = Icons.Default.Gamepad,
                mintPrimary = mintPrimary,
                lightMintBg = lightMintBg,
                onClick = onNavigateToRelax
            )
            MenuGameCard(
                modifier = Modifier.weight(1f),
                title = "Juegos de Memoria",
                icon = Icons.Default.Psychology,
                mintPrimary = mintPrimary,
                lightMintBg = lightMintBg,
                onClick = onNavigateToMemoryTypes
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MenuGameCard(
                modifier = Modifier.weight(1f),
                title = "Chat",
                icon = Icons.Default.Chat,
                mintPrimary = mintPrimary,
                lightMintBg = lightMintBg,
                onClick = { /* Ir a Chat */ }
            )
            MenuGameCard(
                modifier = Modifier.weight(1f),
                title = "Historia",
                icon = Icons.Default.Book,
                mintPrimary = mintPrimary,
                lightMintBg = lightMintBg,
                onClick = onNavigateToStory
            )
        }
    }
}

@Composable
fun MenuGameCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    mintPrimary: Color,
    lightMintBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp) // Fuerza altura uniforme para todas las tarjetas
            .clickable { onClick() }
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(lightMintBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = mintPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BlueDark,
                lineHeight = 18.sp
            )
        }
    }
}