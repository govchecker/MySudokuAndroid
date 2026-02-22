package com.example.mysudoku.model

data class SudokuPuzzle(
    val puzzle: List<Int>,
    val solution: List<Int>
)

class SudokuGenerator {

    /**
     * Generiert ein Sudoku-Rätsel.
     * @param emptyCells Zielanzahl der leeren Zellen.
     * @param maxTechnique Die höchste erlaubte Logik-Technik.
     */
    fun generate(
        emptyCells: Int = 40, 
        maxTechnique: SudokuLogicSolver.Technique = SudokuLogicSolver.Technique.HIDDEN_SINGLE
    ): SudokuPuzzle {
        val grid = Array(9) { IntArray(9) }
        
        // 1. Vollständiges, gültiges Grid erstellen
        fillDiagonal(grid)
        fillRemaining(0, 3, grid)
        
        val solution = grid.flatMap { it.toList() }
        
        // 2. Zellen entfernen, solange es logisch lösbar bleibt
        removeDigitsSmart(grid, emptyCells, maxTechnique)
        
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

        val nums = (1..9).shuffled()
        for (num in nums) {
            if (checkIfSafe(row, col, num, grid)) {
                grid[row][col] = num
                if (fillRemaining(row, col + 1, grid)) return true
                grid[row][col] = 0
            }
        }
        return false
    }

    /**
     * Entfernt Zahlen und prüft nach jedem Schritt, ob das Rätsel noch logisch lösbar ist.
     */
    private fun removeDigitsSmart(
        grid: Array<IntArray>, 
        targetEmpty: Int, 
        maxTechnique: SudokuLogicSolver.Technique
    ) {
        val cellIndices = (0 until 81).shuffled().toMutableList()
        var removedCount = 0
        
        for (idx in cellIndices) {
            if (removedCount >= targetEmpty) break
            
            val r = idx / 9
            val c = idx % 9
            
            if (grid[r][c] != 0) {
                val tempValue = grid[r][c]
                grid[r][c] = 0
                
                // Prüfen, ob das Rätsel noch mit der erlaubten Logik lösbar ist
                val solver = SudokuLogicSolver(grid)
                if (solver.solve(maxTechnique)) {
                    removedCount++
                } else {
                    // Nicht lösbar → Rückgängig machen
                    grid[r][c] = tempValue
                }
            }
        }
    }
}
