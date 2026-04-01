package com.AEAS.dailymemory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AEAS.dailymemory.ui.theme.BlueDark
import com.AEAS.dailymemory.ui.theme.BluePrimary
import com.AEAS.dailymemory.ui.theme.FloatingMascot
import com.AEAS.dailymemory.ui.theme.GradientBackground
import com.AEAS.dailymemory.ui.theme.TextMain
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect

@Composable
fun LoginSignupScreen(
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit = onLoginSuccess,
    authViewModel: AuthViewModel = viewModel()
) {
    var isLogin by remember { mutableStateOf(true) }

    var name by remember { mutableStateOf("") }
    var appe by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConfirm by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) } // NUEVO ESTADO
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            if (isLogin) onLoginSuccess() else onRegisterSuccess()
        }
    }

    GradientBackground {
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(min = 300.dp, max = 380.dp)
                    .wrapContentHeight() // Se ajusta al contenido
                    .shadow(16.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        // CORRECCIÓN: Mantenemos scroll por si acaso en pantallas muy pequeñas
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.align(Alignment.CenterHorizontally)

                    ) {
                        FloatingMascot(size = 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Daily Memory",
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isLogin) "Inicio de sesión" else "Registrarse",
                        color = BluePrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(24.dp))

                    if (!isLogin) {
                        CustomOutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = "Nombre", icon = Icons.Default.Person
                        )
                        Spacer(Modifier.height(12.dp))

                        CustomOutlinedTextField(
                            value = appe, onValueChange = { appe = it },
                            label = "Apellidos", icon = Icons.Default.Person
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    CustomOutlinedTextField(
                        value = username, onValueChange = { username = it },
                        label = "Nombre de usuario", icon = Icons.Default.Person
                    )
                    Spacer(Modifier.height(12.dp))

                    if (!isLogin) {
                        CustomOutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = "Correo electrónico", icon = Icons.Default.Email
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    CustomOutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = "Correo electrónico", icon = Icons.Default.Email
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, focusedLabelColor = BluePrimary)
                    )

                    if (!isLogin) {
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passConfirm,
                            onValueChange = { passConfirm = it },
                            label = { Text("Confirmar contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BluePrimary) },
                            trailingIcon = {
                                val icon = if (passwordConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                                    Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
                                }
                            },
                            visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, focusedLabelColor = BluePrimary)
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { termsAccepted = !termsAccepted }
                        ) {
                            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it }, colors = CheckboxDefaults.colors(checkedColor = BluePrimary))
                            Text("Acepto los términos y condiciones", style = MaterialTheme.typography.bodySmall, color = TextMain)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (isLogin) {
                                if (username.isNotEmpty() && pass.isNotEmpty()) {
                                    authViewModel.login(username, pass)
                                }
                            } else {
                                if (pass == passConfirm && termsAccepted) {
                                    authViewModel.register(email, pass, name, appe, username)
                                }
                            }
                        },
                        enabled = authState !is AuthState.Loading, // Se desactiva mientras carga
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text(
                            text = if (authState is AuthState.Loading) "CARGANDO..."
                            else if (isLogin) "LOGIN" else "SIGN UP",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { isLogin = !isLogin }) {
                        Text(
                            if (isLogin) "¿No tienes cuenta? Regístrate"
                            else "¿Ya tienes cuenta? Inicia sesión",
                            color = BluePrimary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = BluePrimary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            focusedLabelColor = BluePrimary
        )
    )
}