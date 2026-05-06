package com.AEAS.dailymemory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.UserProfileChangeRequest


sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(email: String, pass: String, name: String, appe: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    firebaseUser.updateProfile(profileUpdates).await()

                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = name,
                        appe = appe,
                        username = username,
                        email = email
                    )
                    db.collection("users").document(firebaseUser.uid).set(newUser).await()

                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al registrar")
            }
        }
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val querySnapshot = db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _authState.value = AuthState.Error("El nombre de usuario no existe")
                    return@launch
                }

                val email = querySnapshot.documents[0].getString("email")

                if (email != null) {
                    auth.signInWithEmailAndPassword(email, pass).await()
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Error en los datos del usuario")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}