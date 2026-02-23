package com.example.mysudoku.model

data class SudokuHint(
    val row: Int?,
    val col: Int?,
    val value: Int?,
    val technique: SudokuLogicSolver.Technique,
    val message: String,
    val affectedCells: List<Pair<Int, Int>> = emptyList()
)
