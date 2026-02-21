package com.example.mysudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.mysudoku.ui.SudokuGameScreen
import com.example.mysudoku.ui.SudokuViewModel
import com.example.mysudoku.ui.theme.MySudokuTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SudokuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySudokuTheme {
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SudokuGameScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        onCellClick = viewModel::selectCell,
                        onNumberInput = viewModel::handleInput,
                        onToggleNoteMode = viewModel::toggleNoteMode,
                        onUndo = viewModel::undo,
                        onNewGame = viewModel::startNewGame,
                        onAutoFillNotes = viewModel::autoFillNotes
                    )
                }
            }
        }
    }
}
