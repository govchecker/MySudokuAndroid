package com.example.mysudoku.model

/**
 * Repräsentiert eine einzelne Zelle im Sudoku-Grid.
 */
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val solutionValue: Int = 0, // Der richtige Wert für diese Zelle
    val notes: Set<Int> = emptySet(),
    val isFixed: Boolean = false,
    val isError: Boolean = false
) {
    val boxIndex: Int get() = (row / 3) * 3 + (col / 3)
    
    // Eine Zelle gilt als falsch, wenn ein Wert eingetragen ist, 
    // der nicht der Lösung entspricht ODER ein Duplikat-Fehler vorliegt.
    val isWrong: Boolean get() = value != 0 && value != solutionValue
}
