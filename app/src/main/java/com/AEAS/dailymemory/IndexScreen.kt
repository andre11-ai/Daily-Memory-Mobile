package com.AEAS.dailymemory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.BluePrimary
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground

@Composable
fun IndexScreen(onStartClick: () -> Unit) {
    GradientBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth > 600.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTablet) 48.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier
                        .weight(if (isTablet) 0.6f else 1f)
                        .padding(end = if (isTablet) 32.dp else 0.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Icono",
                            tint = BluePrimary,
                            modifier = Modifier.size(35.sp.value.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Memory",
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 35.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(55.dp))

                    Text(
                        text = "¿Necesitas ayuda con tu mente?",
                        color = BlueDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (isTablet) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineLarge,
                        lineHeight = if (isTablet) 60.sp else 50.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Entrena tu memoria de manera divertida y profesional.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        style = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                        lineHeight = 33.sp
                    )

                    Spacer(modifier = Modifier.height(56.dp))

                    Button(
                        onClick = onStartClick,
                        modifier = Modifier
                            .align(if (isTablet) Alignment.Start else Alignment.CenterHorizontally)
                            .height(60.dp)
                            .widthIn(min = 300.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Empezar ahora", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isTablet) {
                    Box(
                        modifier = Modifier.weight(0.4f),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingMascot(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            size = Dp.Unspecified
                        )
                    }
                }
            }
        }
    }
}