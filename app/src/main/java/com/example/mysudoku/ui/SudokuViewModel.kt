package com.example.mysudoku.ui

import androidx.lifecycle.ViewModel
import com.example.mysudoku.model.SudokuCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SudokuUiState(
    val grid: List<SudokuCell> = emptyList(),
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val selectedNumber: Int? = null,
    val numberCounts: Map<Int, Int> = emptyMap(),
    val isNoteModeEnabled: Boolean = false,
    val history: List<List<SudokuCell>> = emptyList()
)

class SudokuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()
    val canUndo: Boolean get() = _uiState.value.history.isNotEmpty()

    init {
        loadNewGame()
    }

    fun loadNewGame() {
        val board = listOf(
            5, 3, 0, 0, 7, 0, 0, 0, 0, 6, 0, 0, 1, 9, 5, 0, 0, 0, 0, 9, 8, 0, 0, 0, 0, 6, 0, 8, 0, 0, 0, 6, 0, 0, 0, 3, 4, 0, 0, 8, 0, 3, 0, 0, 1, 7, 0, 0, 0, 2, 0, 0, 0, 6, 0, 6, 0, 0, 0, 0, 2, 8, 0, 0, 0, 0, 4, 1, 9, 0, 0, 5, 0, 0, 0, 0, 8, 0, 0, 7, 9
        )
        val initialGrid = board.mapIndexed { index, value ->
            SudokuCell(row = index / 9, col = index % 9, value = value, isFixed = value != 0)
        }
        val validatedGrid = validateGrid(initialGrid)
        _uiState.update { it.copy(grid = validatedGrid, numberCounts = calculateCounts(validatedGrid), history = emptyList()) }
    }

    fun selectCell(row: Int, col: Int) {
        val currentState = _uiState.value
        val activeNumber = currentState.selectedNumber

        if (activeNumber != null) {
            applyInputToCell(row, col, activeNumber)
        } else {
            _uiState.update { it.copy(selectedRow = row, selectedCol = col, selectedNumber = null) }
        }
    }

    fun toggleNoteMode() {
        _uiState.update { it.copy(isNoteModeEnabled = !it.isNoteModeEnabled, selectedNumber = null) }
    }

    fun handleInput(number: Int) {
        val currentState = _uiState.value
        if (currentState.selectedRow != null && currentState.selectedCol != null) {
            applyInputToCell(currentState.selectedRow, currentState.selectedCol, number)
            _uiState.update { it.copy(selectedRow = null, selectedCol = null, selectedNumber = null) }
        } else {
            _uiState.update { it.copy(selectedNumber = if (currentState.selectedNumber == number) null else number) }
        }
    }
    
    fun undo() {
        if (!canUndo) return
        val lastGrid = _uiState.value.history.last()
        val newHistory = _uiState.value.history.dropLast(1)
        _uiState.update {
            it.copy(
                grid = lastGrid,
                history = newHistory,
                numberCounts = calculateCounts(lastGrid)
            )
        }
    }

    private fun applyInputToCell(row: Int, col: Int, number: Int) {
        val currentGrid = _uiState.value.grid
        if (!currentGrid[row * 9 + col].isFixed) {
             val newHistory = _uiState.value.history + listOf(currentGrid)
            _uiState.update { it.copy(history = newHistory) }
        }

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
        if (targetCell.isFixed) return

        val newGrid = currentState.grid.mapIndexed { index, cell ->
            if (index == targetIndex) {
                cell.copy(value = if (cell.value == number) 0 else number, notes = emptySet())
            } else {
                cell
            }
        }

        val validatedGrid = validateGrid(newGrid)
        _uiState.update {
            it.copy(
                grid = validatedGrid,
                numberCounts = calculateCounts(validatedGrid)
            )
        }
    }

    private fun toggleNote(row: Int, col: Int, number: Int) {
        val currentState = _uiState.value
        val targetIndex = row * 9 + col
        val targetCell = currentState.grid[targetIndex]
        if (targetCell.isFixed || targetCell.value != 0) return
        val newNotes = if (targetCell.notes.contains(number)) targetCell.notes - number else targetCell.notes + number
        val newGrid = currentState.grid.toMutableList().apply {
            this[targetIndex] = targetCell.copy(notes = newNotes)
        }
        _uiState.update { it.copy(grid = newGrid) }
    }

    private fun validateGrid(grid: List<SudokuCell>): List<SudokuCell> {
        return grid.map { cell ->
            if (cell.value == 0) {
                cell.copy(isError = false)
            } else {
                val hasError = grid.any { other ->
                    other !== cell && other.value == cell.value &&
                    (other.row == cell.row || other.col == cell.col || other.boxIndex == cell.boxIndex)
                }
                cell.copy(isError = hasError)
            }
        }
    }

    private fun calculateCounts(grid: List<SudokuCell>): Map<Int, Int> {
        return grid.filter { it.value != 0 }.groupBy { it.value }.mapValues { it.value.size }
    }
}
