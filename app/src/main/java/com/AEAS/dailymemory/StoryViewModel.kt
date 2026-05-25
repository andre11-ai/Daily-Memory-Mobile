package com.AEAS.dailymemory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoryViewModel : ViewModel() {
    private val _currentLevel = MutableStateFlow(3)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _showHelpModal = MutableStateFlow(true)
    val showHelpModal: StateFlow<Boolean> = _showHelpModal.asStateFlow()

    fun dismissHelp() { _showHelpModal.value = false }
    fun openHelp() { _showHelpModal.value = true }
}