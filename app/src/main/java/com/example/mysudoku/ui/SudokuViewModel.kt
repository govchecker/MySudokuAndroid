package com.example.mysudoku.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mysudoku.model.SudokuCell
import com.example.mysudoku.model.SudokuGenerator
import com.example.mysudoku.model.SudokuHint
import com.example.mysudoku.model.SudokuLogicSolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class Difficulty(val emptyCells: Int, val maxTechnique: SudokuLogicSolver.Technique) {
    EASY(35, SudokuLogicSolver.Technique.NAKED_SINGLE),
    MEDIUM(45, SudokuLogicSolver.Technique.HIDDEN_SINGLE),
    HARD(55, SudokuLogicSolver.Technique.POINTING_PAIRS),
    EXPERT(60, SudokuLogicSolver.Technique.NAKED_PAIR)
}

data class AnimationEvent(
    val rows: List<Int> = emptyList(),
    val cols: List<Int> = emptyList(),
    val boxes: List<Int> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

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
    val timerSeconds: Long = 0,
    val showDifficultyDialog: Boolean = false,
    val currentHint: SudokuHint? = null,
    val animationEvent: AnimationEvent? = null
)

class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()
    val canUndo: Boolean get() = _uiState.value.history.isNotEmpty()

    private val generator = SudokuGenerator()
    private var timerJob: Job? = null
    private val prefs = application.getSharedPreferences("sudoku_prefs", Context.MODE_PRIVATE)

    init {
        loadGameState()
    }

    private fun loadGameState() {
        val savedDifficulty = prefs.getString("last_difficulty", Difficulty.MEDIUM.name)?.let {
            Difficulty.valueOf(it)
        } ?: Difficulty.MEDIUM

        val savedGridJson = prefs.getString("saved_grid", null)
        if (savedGridJson != null && !prefs.getBoolean("is_game_won", false)) {
            try {
                val grid = parseGrid(savedGridJson)
                val timer = prefs.getLong("timer_seconds", 0)
                _uiState.update { 
                    it.copy(
                        grid = grid,
                        difficulty = savedDifficulty,
                        timerSeconds = timer,
                        numberCounts = calculateCounts(grid),
                        showDifficultyDialog = false
                    )
                }
                startTimer()
            } catch (_: Exception) {
                _uiState.update { it.copy(difficulty = savedDifficulty, showDifficultyDialog = true) }
            }
        } else {
            _uiState.update { it.copy(difficulty = savedDifficulty, showDifficultyDialog = true) }
        }
    }

    private fun saveGameState() {
        val state = _uiState.value
        if (state.grid.isEmpty()) return

        val gridJson = serializeGrid(state.grid)
        prefs.edit().apply {
            putString("saved_grid", gridJson)
            putString("last_difficulty", state.difficulty.name)
            putLong("timer_seconds", state.timerSeconds)
            putBoolean("is_game_won", state.isGameWon)
            apply()
        }
    }

    private fun serializeGrid(grid: List<SudokuCell>): String {
        val array = JSONArray()
        grid.forEach { cell ->
            val obj = JSONObject().apply {
                put("r", cell.row)
                put("c", cell.col)
                put("v", cell.value)
                put("sv", cell.solutionValue)
                put("if", cell.isFixed)
                put("ie", cell.isError)
                put("n", JSONArray(cell.notes.toList()))
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseGrid(json: String): List<SudokuCell> {
        val array = JSONArray(json)
        val list = mutableListOf<SudokuCell>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val notesArray = obj.getJSONArray("n")
            val notes = mutableSetOf<Int>()
            for (j in 0 until notesArray.length()) {
                notes.add(notesArray.getInt(j))
            }
            list.add(SudokuCell(
                row = obj.getInt("r"),
                col = obj.getInt("c"),
                value = obj.getInt("v"),
                solutionValue = obj.getInt("sv"),
                isFixed = obj.getBoolean("if"),
                isError = obj.getBoolean("ie"),
                notes = notes
            ))
        }
        return list
    }

    fun showNewGameDialog() {
        _uiState.update { it.copy(showDifficultyDialog = true) }
    }

    fun dismissNewGameDialog() {
        if (_uiState.value.grid.isNotEmpty()) {
            _uiState.update { it.copy(showDifficultyDialog = false) }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        timerJob?.cancel()
        _uiState.update { it.copy(showDifficultyDialog = false, currentHint = null, animationEvent = null) }
        
        viewModelScope.launch {
            val puzzle = generator.generate(difficulty.emptyCells, difficulty.maxTechnique)
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
                    timerSeconds = 0,
                    currentHint = null,
                    animationEvent = null
                ) 
            }
            saveGameState()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
                if (_uiState.value.timerSeconds % 5 == 0L) {
                    saveGameState()
                }
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
            _uiState.update { it.copy(selectedRow = row, selectedCol = col, selectedNumber = null, currentHint = null) }
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
            _uiState.update { it.copy(selectedNumber = if (currentState.selectedNumber == number) null else number, currentHint = null) }
        }
    }
    
    fun onAnimationFinished() {
        _uiState.update { it.copy(animationEvent = null) }
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
                isGameWon = checkWin(lastGrid),
                currentHint = null,
                animationEvent = null
            )
        }
        saveGameState()
    }

    private fun applyInputToCell(row: Int, col: Int, number: Int) {
        val currentGrid = _uiState.value.grid
        val targetIndex = row * 9 + col
        if (currentGrid[targetIndex].isFixed) return

        val newHistory = _uiState.value.history + listOf(currentGrid)
        _uiState.update { it.copy(history = newHistory, currentHint = null) }

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
        
        if (targetCell.value == number) {
            val newGrid = currentState.grid.mapIndexed { index, cell ->
                if (index == targetIndex) cell.copy(value = 0, notes = emptySet()) else cell
            }
            val validatedGrid = validateGrid(newGrid)
            _uiState.update {
                it.copy(
                    grid = validatedGrid,
                    numberCounts = calculateCounts(validatedGrid),
                    isGameWon = false
                )
            }
            saveGameState()
            return
        }

        val oldCompletedRows = (0..8).filter { r -> isAreaComplete(currentState.grid, "row", r) }.toSet()
        val oldCompletedCols = (0..8).filter { c -> isAreaComplete(currentState.grid, "col", c) }.toSet()
        val oldCompletedBoxes = (0..8).filter { b -> isAreaComplete(currentState.grid, "box", b) }.toSet()

        var newGrid = currentState.grid.mapIndexed { index, cell ->
            if (index == targetIndex) {
                cell.copy(value = number, notes = emptySet())
            } else {
                cell
            }
        }

        // Smart Notes: Clear notes for the entered number in same row, column, and box
        if (number != 0) {
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
        
        val newCompletedRows = (0..8).filter { r -> isAreaComplete(validatedGrid, "row", r) }.toSet()
        val newCompletedCols = (0..8).filter { c -> isAreaComplete(validatedGrid, "col", c) }.toSet()
        val newCompletedBoxes = (0..8).filter { b -> isAreaComplete(validatedGrid, "box", b) }.toSet()

        val newlyCompletedRows = (newCompletedRows - oldCompletedRows).toList()
        val newlyCompletedCols = (newCompletedCols - oldCompletedCols).toList()
        val newlyCompletedBoxes = (newCompletedBoxes - oldCompletedBoxes).toList()

        val animEvent = if (newlyCompletedRows.isNotEmpty() || newlyCompletedCols.isNotEmpty() || newlyCompletedBoxes.isNotEmpty()) {
            AnimationEvent(newlyCompletedRows, newlyCompletedCols, newlyCompletedBoxes)
        } else null

        val isWon = checkWin(validatedGrid)
        if (isWon) timerJob?.cancel()

        _uiState.update {
            it.copy(
                grid = validatedGrid,
                numberCounts = calculateCounts(validatedGrid),
                isGameWon = isWon,
                animationEvent = animEvent
            )
        }
        saveGameState()
    }

    private fun isAreaComplete(grid: List<SudokuCell>, type: String, index: Int): Boolean {
        if (grid.isEmpty()) return false
        val areaCells = when (type) {
            "row" -> grid.filter { it.row == index }
            "col" -> grid.filter { it.col == index }
            "box" -> grid.filter { it.boxIndex == index }
            else -> emptyList()
        }
        return areaCells.size == 9 && areaCells.all { it.value != 0 && !it.isError && !it.isWrong }
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
        saveGameState()
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
        saveGameState()
    }

    fun requestHint() {
        val gridArray = Array(9) { r ->
            IntArray(9) { c ->
                _uiState.value.grid[r * 9 + c].value
            }
        }
        val solver = SudokuLogicSolver(gridArray)
        val hint = solver.findNextHint()
        _uiState.update { it.copy(currentHint = hint) }
    }

    fun clearHint() {
        _uiState.update { it.copy(currentHint = null) }
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
        return grid.isNotEmpty() && grid.all { it.value != 0 && it.value == it.solutionValue }
    }

    private fun calculateCounts(grid: List<SudokuCell>): Map<Int, Int> {
        return grid.filter { it.value != 0 }.groupBy { it.value }.mapValues { it.value.size }
    }
}
