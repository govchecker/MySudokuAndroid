package com.example.mysudoku.model

/**
 * Repräsentiert eine einzelne Zelle im Sudoku-Grid.
 */
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val notes: Set<Int> = emptySet(),
    val isFixed: Boolean = false,
    val isError: Boolean = false // NEU: Markiert, ob die Zahl gegen Sudoku-Regeln verstößt
) {
    val boxIndex: Int get() = (row / 3) * 3 + (col / 3)
}
