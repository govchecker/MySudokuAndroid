package com.example.mysudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysudoku.model.SudokuCell
import com.example.mysudoku.model.SudokuGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Difficulty(val emptyCells: Int) {
    EASY(30),    // Etwas mehr als vorher (25), um nicht zu trivial zu sein
    MEDIUM(42),  // Erhöht von 35 - erfordert mehr Aufmerksamkeit, aber meist ohne komplexe Techniken
    HARD(54)     // Erhöht von 50 - definitiv eine Herausforderung
}

data class SudokuUiState(
    val grid: List<SudokuCell> = emptyList(),
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val selectedNumber: Int? = null,
    val numberCounts: Map<Int, Int> = emptyMap(),
    val isNoteModeEnabled: Boolean = false,
    val history: List<List<SudokuCell>> = emptyList(),
    val isGameWon: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val timerSeconds: Long = 0
)

class SudokuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()
    val canUndo: Boolean get() = _uiState.value.history.isNotEmpty()

    private val generator = SudokuGenerator()
    private var timerJob: Job? = null

    init {
        startNewGame(Difficulty.MEDIUM)
    }

    fun startNewGame(difficulty: Difficulty) {
        timerJob?.cancel()
        val puzzle = generator.generate(difficulty.emptyCells)
        val initialGrid = puzzle.puzzle.mapIndexed { index, value ->
            SudokuCell(
                row = index / 9, 
                col = index % 9, 
                value = value, 
                solutionValue = puzzle.solution[index],
                isFixed = value != 0
            )
        }
        val validatedGrid = validateGrid(initialGrid)
        _uiState.update { 
            it.copy(
                grid = validatedGrid, 
                numberCounts = calculateCounts(validatedGrid), 
                history = emptyList(),
                isGameWon = false,
                difficulty = difficulty,
                selectedRow = null,
                selectedCol = null,
                selectedNumber = null,
                timerSeconds = 0
            ) 
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
            }
        }
    }

    fun selectCell(row: Int, col: Int) {
        if (_uiState.value.isGameWon) return
        val currentState = _uiState.value
        val activeNumber = currentState.selectedNumber

        if (activeNumber != null) {
            applyInputToCell(row, col, activeNumber)
        } else {
            _uiState.update { it.copy(selectedRow = row, selectedCol = col, selectedNumber = null) }
        }
    }

    fun toggleNoteMode() {
        if (_uiState.value.isGameWon) return
        _uiState.update { it.copy(isNoteModeEnabled = !it.isNoteModeEnabled, selectedNumber = null) }
    }

    fun handleInput(number: Int) {
        if (_uiState.value.isGameWon) return
        val currentState = _uiState.value
        if (currentState.selectedRow != null && currentState.selectedCol != null) {
            applyInputToCell(currentState.selectedRow, currentState.selectedCol, number)
            _uiState.update { it.copy(selectedRow = null, selectedCol = null, selectedNumber = null) }
        } else {
            _uiState.update { it.copy(selectedNumber = if (currentState.selectedNumber == number) null else number) }
        }
    }
    
    fun undo() {
        if (!canUndo || _uiState.value.isGameWon) return
        val lastGrid = _uiState.value.history.last()
        val newHistory = _uiState.value.history.dropLast(1)
        _uiState.update {
            it.copy(
                grid = lastGrid,
                history = newHistory,
                numberCounts = calculateCounts(lastGrid),
                isGameWon = checkWin(lastGrid)
            )
        }
    }

    private fun applyInputToCell(row: Int, col: Int, number: Int) {
        val currentGrid = _uiState.value.grid
        val targetIndex = row * 9 + col
        if (currentGrid[targetIndex].isFixed) return

        val newHistory = _uiState.value.history + listOf(currentGrid)
        _uiState.update { it.copy(history = newHistory) }

        if (_uiState.value.isNoteModeEnabled) {
            toggleNote(row, col, number)
        } else {
            enterNumber(row, col, number)
        }
    }

    private fun enterNumber(row: Int, col: Int, number: Int) {
        val currentState = _uiState.value
        val targetIndex = row * 9 + col
        val targetCell = currentState.grid[targetIndex]
        
        val isCorrectEntry = number == targetCell.solutionValue

        var newGrid = currentState.grid.mapIndexed { index, cell ->
            if (index == targetIndex) {
                cell.copy(value = if (cell.value == number) 0 else number, notes = emptySet())
            } else {
                cell
            }
        }

        if (isCorrectEntry && number != 0) {
            val targetBoxIndex = (row / 3) * 3 + (col / 3)
            newGrid = newGrid.map { cell ->
                if (cell.row == row || cell.col == col || cell.boxIndex == targetBoxIndex) {
                    cell.copy(notes = cell.notes - number)
                } else {
                    cell
                }
            }
        }

        val validatedGrid = validateGrid(newGrid)
        val isWon = checkWin(validatedGrid)
        
        if (isWon) timerJob?.cancel()

        _uiState.update {
            it.copy(
                grid = validatedGrid,
                numberCounts = calculateCounts(validatedGrid),
                isGameWon = isWon
            )
        }
    }

    private fun toggleNote(row: Int, col: Int, number: Int) {
        val currentState = _uiState.value
        val targetIndex = row * 9 + col
        val targetCell = currentState.grid[targetIndex]
        if (targetCell.value != 0) return
        
        val newNotes = if (targetCell.notes.contains(number)) targetCell.notes - number else targetCell.notes + number
        val newGrid = currentState.grid.toMutableList().apply {
            this[targetIndex] = targetCell.copy(notes = newNotes)
        }
        _uiState.update { it.copy(grid = newGrid) }
    }

    fun autoFillNotes() {
        val currentGrid = _uiState.value.grid
        val newHistory = _uiState.value.history + listOf(currentGrid)
        
        val newGrid = currentGrid.map { cell ->
            if (cell.value == 0) {
                val possibleValues = (1..9).filter { num ->
                    val inRow = currentGrid.any { it.row == cell.row && it.value == num }
                    val inCol = currentGrid.any { it.col == cell.col && it.value == num }
                    val inBox = currentGrid.any { it.boxIndex == cell.boxIndex && it.value == num }
                    !inRow && !inCol && !inBox
                }.toSet()
                cell.copy(notes = possibleValues)
            } else {
                cell
            }
        }
        
        _uiState.update { it.copy(grid = newGrid, history = newHistory) }
    }

    private fun validateGrid(grid: List<SudokuCell>): List<SudokuCell> {
        return grid.map { cell ->
            if (cell.value == 0) {
                cell.copy(isError = false)
            } else {
                val hasDuplicate = grid.any { other ->
                    other !== cell && other.value == cell.value &&
                    (other.row == cell.row || other.col == cell.col || other.boxIndex == cell.boxIndex)
                }
                cell.copy(isError = hasDuplicate)
            }
        }
    }

    private fun checkWin(grid: List<SudokuCell>): Boolean {
        return grid.all { it.value != 0 && it.value == it.solutionValue }
    }

    private fun calculateCounts(grid: List<SudokuCell>): Map<Int, Int> {
        return grid.filter { it.value != 0 }.groupBy { it.value }.mapValues { it.value.size }
    }
}
