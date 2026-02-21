package com.example.mysudoku.model

import kotlin.random.Random

data class SudokuPuzzle(
    val puzzle: List<Int>,
    val solution: List<Int>
)

class SudokuGenerator {

    fun generate(emptyCells: Int = 40): SudokuPuzzle {
        val grid = Array(9) { IntArray(9) { 0 } }
        
        fillDiagonal(grid)
        fillRemaining(0, 3, grid)
        
        val solution = grid.flatMap { it.toList() }
        
        // Symmetrisches Löschen für bessere Qualität
        removeDigitsSymmetrically(grid, emptyCells)
        
        val puzzle = grid.flatMap { it.toList() }
        
        return SudokuPuzzle(puzzle, solution)
    }

    private fun fillDiagonal(grid: Array<IntArray>) {
        for (i in 0 until 9 step 3) {
            fillBox(i, i, grid)
        }
    }

    private fun fillBox(row: Int, col: Int, grid: Array<IntArray>) {
        val nums = (1..9).shuffled()
        var idx = 0
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                grid[row + i][col + j] = nums[idx++]
            }
        }
    }

    private fun checkIfSafe(i: Int, j: Int, num: Int, grid: Array<IntArray>): Boolean {
        return (unUsedInRow(i, num, grid) &&
                unUsedInCol(j, num, grid) &&
                unUsedInBox(i - i % 3, j - j % 3, num, grid))
    }

    private fun unUsedInRow(i: Int, num: Int, grid: Array<IntArray>): Boolean {
        for (j in 0 until 9) {
            if (grid[i][j] == num) return false
        }
        return true
    }

    private fun unUsedInCol(j: Int, num: Int, grid: Array<IntArray>): Boolean {
        for (i in 0 until 9) {
            if (grid[i][j] == num) return false
        }
        return true
    }

    private fun unUsedInBox(rowStart: Int, colStart: Int, num: Int, grid: Array<IntArray>): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (grid[rowStart + i][colStart + j] == num) return false
            }
        }
        return true
    }

    private fun fillRemaining(i: Int, j: Int, grid: Array<IntArray>): Boolean {
        var row = i
        var col = j

        if (col >= 9 && row < 8) {
            row += 1
            col = 0
        }
        if (row >= 9 && col >= 9) return true

        if (row < 3) {
            if (col < 3) col = 3
        } else if (row < 6) {
            if (col == (row / 3) * 3) col += 3
        } else {
            if (col == 6) {
                row += 1
                col = 0
                if (row >= 9) return true
            }
        }

        for (num in 1..9) {
            if (checkIfSafe(row, col, num, grid)) {
                grid[row][col] = num
                if (fillRemaining(row, col + 1, grid)) return true
                grid[row][col] = 0
            }
        }
        return false
    }

    private fun removeDigitsSymmetrically(grid: Array<IntArray>, count: Int) {
        var remaining = count
        val attempts = 100 // Sicherheits-Limit
        var currentAttempt = 0
        
        while (remaining > 0 && currentAttempt < attempts) {
            val cellId = Random.nextInt(41) // Nur bis zur Hälfte gehen
            val r = cellId / 9
            val c = cellId % 9
            
            if (grid[r][c] != 0) {
                grid[r][c] = 0
                remaining--
                
                // Symmetrisches Gegenstück löschen
                val oppR = 8 - r
                val oppC = 8 - c
                if (grid[oppR][oppC] != 0 && remaining > 0) {
                    grid[oppR][oppC] = 0
                    remaining--
                }
            }
            currentAttempt++
        }
    }
}
