package com.example.mysudoku.model

class SudokuLogicSolver(private val initialGrid: Array<IntArray>) {

    private val grid = Array(9) { r -> initialGrid[r].copyOf() }
    private val candidates = Array(9) { Array(9) { mutableSetOf<Int>() } }

    enum class Technique {
        NAKED_SINGLE,
        HIDDEN_SINGLE,
        POINTING_PAIRS,
        NAKED_PAIR
    }

    private fun initCandidates() {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0) {
                    candidates[r][c].addAll(getPossibleValues(r, c))
                } else {
                    candidates[r][c].clear()
                }
            }
        }
    }

    private fun getPossibleValues(r: Int, c: Int): List<Int> {
        val possible = mutableListOf<Int>()
        for (v in 1..9) {
            if (isSafe(r, c, v)) {
                possible.add(v)
            }
        }
        return possible
    }

    private fun isSafe(r: Int, c: Int, v: Int): Boolean {
        for (i in 0 until 9) {
            if (grid[r][i] == v || grid[i][c] == v) return false
        }
        val startR = (r / 3) * 3
        val startC = (c / 3) * 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (grid[startR + i][startC + j] == v) return false
            }
        }
        return true
    }

    /**
     * Versucht das Sudoku rein logisch zu lösen.
     * @param maxTechnique Die höchste erlaubte Technik für diesen Schwierigkeitsgrad.
     * @return true, wenn das Sudoku vollständig gelöst wurde.
     */
    fun solve(maxTechnique: Technique): Boolean {
        initCandidates()
        var changed: Boolean
        do {
            changed = false
            
            // 1. Naked Singles
            if (applyNakedSingles()) {
                changed = true
                continue
            }

            // 2. Hidden Singles
            if (applyHiddenSingles()) {
                changed = true
                continue
            }

            if (maxTechnique.ordinal >= Technique.POINTING_PAIRS.ordinal) {
                if (applyPointingPairs()) {
                    changed = true
                    continue
                }
            }

            if (maxTechnique.ordinal >= Technique.NAKED_PAIR.ordinal) {
                if (applyNakedPairs()) {
                    changed = true
                    continue
                }
            }

        } while (changed)

        return isSolved()
    }

    private fun isSolved(): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0) return false
            }
        }
        return true
    }

    private fun applyNakedSingles(): Boolean {
        var changed = false
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0 && candidates[r][c].size == 1) {
                    val value = candidates[r][c].first()
                    setValue(r, c, value)
                    changed = true
                }
            }
        }
        return changed
    }

    private fun applyHiddenSingles(): Boolean {
        var changed = false
        for (v in 1..9) {
            // Check rows
            for (r in 0 until 9) {
                val possibleCols = (0 until 9).filter { c -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleCols.size == 1) {
                    setValue(r, possibleCols[0], v)
                    changed = true
                }
            }
            // Check columns
            for (c in 0 until 9) {
                val possibleRows = (0 until 9).filter { r -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleRows.size == 1) {
                    setValue(possibleRows[0], c, v)
                    changed = true
                }
            }
            // Check boxes
            for (b in 0 until 9) {
                val startR = (b / 3) * 3
                val startC = (b % 3) * 3
                val possibleCells = mutableListOf<Pair<Int, Int>>()
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        val r = startR + i
                        val c = startC + j
                        if (grid[r][c] == 0 && candidates[r][c].contains(v)) {
                            possibleCells.add(r to c)
                        }
                    }
                }
                if (possibleCells.size == 1) {
                    setValue(possibleCells[0].first, possibleCells[0].second, v)
                    changed = true
                }
            }
        }
        return changed
    }

    private fun applyPointingPairs(): Boolean {
        var changed = false
        for (v in 1..9) {
            for (b in 0 until 9) {
                val startR = (b / 3) * 3
                val startC = (b % 3) * 3
                val possibleCells = mutableListOf<Pair<Int, Int>>()
                for (i in 0 until 3) {
                    for (j in 0 until 3) {
                        val r = startR + i
                        val c = startC + j
                        if (grid[r][c] == 0 && candidates[r][c].contains(v)) {
                            possibleCells.add(r to c)
                        }
                    }
                }

                if (possibleCells.size >= 2) {
                    val sameRow = possibleCells.all { it.first == possibleCells[0].first }
                    val sameCol = possibleCells.all { it.second == possibleCells[0].second }

                    if (sameRow) {
                        val r = possibleCells[0].first
                        for (c in 0 until 9) {
                            if (c < startC || c >= startC + 3) {
                                if (candidates[r][c].remove(v)) changed = true
                            }
                        }
                    }
                    if (sameCol) {
                        val c = possibleCells[0].second
                        for (r in 0 until 9) {
                            if (r < startR || r >= startR + 3) {
                                if (candidates[r][c].remove(v)) changed = true
                            }
                        }
                    }
                }
            }
        }
        return changed
    }

    private fun applyNakedPairs(): Boolean {
        // Implementation for Naked Pairs could be added here for higher difficulty.
        // Returning false for now to avoid 'unused variable' warnings.
        return false
    }

    private fun setValue(r: Int, c: Int, v: Int) {
        grid[r][c] = v
        candidates[r][c].clear()
        // Update candidates in row, col, and box
        for (i in 0 until 9) {
            candidates[r][i].remove(v)
            candidates[i][c].remove(v)
        }
        val startR = (r / 3) * 3
        val startC = (c / 3) * 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                candidates[startR + i][startC + j].remove(v)
            }
        }
    }
}
