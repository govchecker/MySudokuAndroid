@file:Suppress("SpellCheckingInspection")

package com.example.mysudoku.model

class SudokuLogicSolver(private val initialGrid: Array<IntArray>) {

    private val grid = Array(9) { r -> initialGrid[r].copyOf() }
    private val candidates = Array(9) { Array(9) { mutableSetOf<Int>() } }

    enum class Technique {
        NAKED_SINGLE,
        HIDDEN_SINGLE,
        POINTING_PAIRS,
        NAKED_PAIR,
        X_WING
    }

    init {
        initCandidates()
    }

    private fun initCandidates() {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0) {
                    candidates[r][c].clear()
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

    fun findNextHint(): SudokuHint? {
        // Try finding hints in order of technique complexity
        findNakedSingleHint()?.let { return it }
        findHiddenSingleHint()?.let { return it }
        findPointingPairsHint()?.let { return it }
        findNakedPairsHint()?.let { return it }
        findXWingHint()?.let { return it }

        return null // No logical step found
    }

    private fun findNakedSingleHint(): SudokuHint? {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (grid[r][c] == 0 && candidates[r][c].size == 1) {
                    val value = candidates[r][c].first()
                    return SudokuHint(
                        row = r,
                        col = c,
                        value = value,
                        technique = Technique.NAKED_SINGLE,
                        message = "In dieser Zelle ist nur eine Zahl möglich: $value.",
                        affectedCells = listOf(r to c)
                    )
                }
            }
        }
        return null
    }

    private fun findHiddenSingleHint(): SudokuHint? {
        for (v in 1..9) {
            // Check rows
            for (r in 0 until 9) {
                val possibleCols = (0 until 9).filter { c -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleCols.size == 1) {
                    val c = possibleCols[0]
                    return SudokuHint(r, c, v, Technique.HIDDEN_SINGLE, "In dieser Reihe kann die $v nur hier stehen.", listOf(r to c))
                }
            }
            // Check columns
            for (c in 0 until 9) {
                val possibleRows = (0 until 9).filter { r -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleRows.size == 1) {
                    val r = possibleRows[0]
                    return SudokuHint(r, c, v, Technique.HIDDEN_SINGLE, "In dieser Spalte kann die $v nur hier stehen.", listOf(r to c))
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
                    val (r, c) = possibleCells[0]
                    return SudokuHint(r, c, v, Technique.HIDDEN_SINGLE, "In diesem Block kann die $v nur hier stehen.", listOf(r to c))
                }
            }
        }
        return null
    }

    private fun findPointingPairsHint(): SudokuHint? {
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

                if (possibleCells.size in 2..3) {
                    val sameRow = possibleCells.all { it.first == possibleCells[0].first }
                    val sameCol = possibleCells.all { it.second == possibleCells[0].second }

                    if (sameRow) {
                        val r = possibleCells[0].first
                        val affected = (0 until 9).filter { c -> (c < startC || c >= startC + 3) && candidates[r][c].contains(v) }
                        if (affected.isNotEmpty()) {
                            return SudokuHint(null, null, v, Technique.POINTING_PAIRS, "Pointing Pair: Die $v in diesem Block ist auf eine Reihe beschränkt. Sie kann aus dem Rest der Reihe entfernt werden.", possibleCells)
                        }
                    }
                    if (sameCol) {
                        val c = possibleCells[0].second
                        val affected = (0 until 9).filter { r -> (r < startR || r >= startR + 3) && candidates[r][c].contains(v) }
                        if (affected.isNotEmpty()) {
                            return SudokuHint(null, null, v, Technique.POINTING_PAIRS, "Pointing Pair: Die $v in diesem Block ist auf eine Spalte beschränkt. Sie kann aus dem Rest der Spalte entfernt werden.", possibleCells)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findNakedPairsHint(): SudokuHint? {
        // Rows
        for (r in 0 until 9) {
            val pairs = (0 until 9).filter { c -> candidates[r][c].size == 2 }
            if (pairs.size >= 2) {
                for (i in pairs.indices) {
                    for (j in i + 1 until pairs.size) {
                        val c1 = pairs[i]
                        val c2 = pairs[j]
                        if (candidates[r][c1] == candidates[r][c2]) {
                            val vals = candidates[r][c1]
                            val affected = (0 until 9).filter { c -> c != c1 && c != c2 && (candidates[r][c].intersect(vals).isNotEmpty()) }
                            if (affected.isNotEmpty()) {
                                return SudokuHint(null, null, null, Technique.NAKED_PAIR, "Naked Pair: Diese zwei Zellen in der Reihe enthalten nur die Kandidaten ${vals.joinToString("/")}. Diese können aus dem Rest der Reihe entfernt werden.", listOf(r to c1, r to c2))
                            }
                        }
                    }
                }
            }
        }
        // Columns
        for (c in 0 until 9) {
            val pairs = (0 until 9).filter { r -> candidates[r][c].size == 2 }
            if (pairs.size >= 2) {
                for (i in pairs.indices) {
                    for (j in i + 1 until pairs.size) {
                        val r1 = pairs[i]
                        val r2 = pairs[j]
                        if (candidates[r1][c] == candidates[r2][c]) {
                            val vals = candidates[r1][c]
                            val affected = (0 until 9).filter { r -> r != r1 && r != r2 && (candidates[r][c].intersect(vals).isNotEmpty()) }
                            if (affected.isNotEmpty()) {
                                return SudokuHint(null, null, null, Technique.NAKED_PAIR, "Naked Pair: Diese zwei Zellen in der Spalte enthalten nur die Kandidaten ${vals.joinToString("/")}. Diese können aus dem Rest der Spalte entfernt werden.", listOf(r1 to c, r2 to c))
                            }
                        }
                    }
                }
            }
        }
        // Boxes
        for (b in 0 until 9) {
            val startR = (b / 3) * 3
            val startC = (b % 3) * 3
            val boxCells = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    boxCells.add(startR + i to startC + j)
                }
            }
            val pairs = boxCells.filter { (r, c) -> candidates[r][c].size == 2 }
            if (pairs.size >= 2) {
                for (i in pairs.indices) {
                    for (j in i + 1 until pairs.size) {
                        val (r1, c1) = pairs[i]
                        val (r2, c2) = pairs[j]
                        if (candidates[r1][c1] == candidates[r2][c2]) {
                            val vals = candidates[r1][c1]
                            val affected = boxCells.filter { (r, c) -> (r != r1 || c != c1) && (r != r2 || c != c2) && (candidates[r][c].intersect(vals).isNotEmpty()) }
                            if (affected.isNotEmpty()) {
                                return SudokuHint(null, null, null, Technique.NAKED_PAIR, "Naked Pair: Diese zwei Zellen im Block enthalten nur die Kandidaten ${vals.joinToString("/")}. Diese können aus dem Rest des Blocks entfernt werden.", listOf(r1 to c1, r2 to c2))
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findXWingHint(): SudokuHint? {
        for (v in 1..9) {
            // Rows X-Wing
            val rowPositions = Array(9) { r -> (0 until 9).filter { c -> candidates[r][c].contains(v) } }
            for (r1 in 0 until 9) {
                if (rowPositions[r1].size == 2) {
                    for (r2 in r1 + 1 until 9) {
                        if (rowPositions[r2] == rowPositions[r1]) {
                            val c1 = rowPositions[r1][0]
                            val c2 = rowPositions[r1][1]
                            val affected = (0 until 9).filter { r -> r != r1 && r != r2 && (candidates[r][c1].contains(v) || candidates[r][c2].contains(v)) }
                            if (affected.isNotEmpty()) {
                                return SudokuHint(null, null, v, Technique.X_WING, "X-Wing: Die $v ist in den Reihen $r1/$r2 auf die Spalten $c1/$c2 beschränkt. Sie kann aus dem Rest dieser Spalten entfernt werden.", listOf(r1 to c1, r1 to c2, r2 to c1, r2 to c2))
                            }
                        }
                    }
                }
            }
            // Columns X-Wing
            val colPositions = Array(9) { c -> (0 until 9).filter { r -> candidates[r][c].contains(v) } }
            for (c1 in 0 until 9) {
                if (colPositions[c1].size == 2) {
                    for (c2 in c1 + 1 until 9) {
                        if (colPositions[c2] == colPositions[c1]) {
                            val r1 = colPositions[c1][0]
                            val r2 = colPositions[c1][1]
                            val affected = (0 until 9).filter { c -> c != c1 && c != c2 && (candidates[r1][c].contains(v) || candidates[r2][c].contains(v)) }
                            if (affected.isNotEmpty()) {
                                return SudokuHint(null, null, v, Technique.X_WING, "X-Wing: Die $v ist in den Spalten $c1/$c2 auf die Reihen $r1/$r2 beschränkt. Sie kann aus dem Rest dieser Reihen entfernt werden.", listOf(r1 to c1, r1 to c2, r2 to c1, r2 to c2))
                            }
                        }
                    }
                }
            }
        }
        return null
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
            
            if (applyNakedSingles()) {
                changed = true
                continue
            }

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

            if (maxTechnique.ordinal >= Technique.X_WING.ordinal) {
                if (applyXWing()) {
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
            for (r in 0 until 9) {
                val possibleCols = (0 until 9).filter { c -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleCols.size == 1) {
                    setValue(r, possibleCols[0], v)
                    changed = true
                }
            }
            for (c in 0 until 9) {
                val possibleRows = (0 until 9).filter { r -> grid[r][c] == 0 && candidates[r][c].contains(v) }
                if (possibleRows.size == 1) {
                    setValue(possibleRows[0], c, v)
                    changed = true
                }
            }
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
        var changed = false
        // Rows
        for (r in 0 until 9) {
            val pairs = (0 until 9).filter { candidates[r][it].size == 2 }
            for (i in pairs.indices) {
                for (j in i + 1 until pairs.size) {
                    val c1 = pairs[i]
                    val c2 = pairs[j]
                    if (candidates[r][c1] == candidates[r][c2]) {
                        val vals = candidates[r][c1]
                        for (c in 0 until 9) {
                            if (c != c1 && c != c2) {
                                if (candidates[r][c].removeAll(vals)) changed = true
                            }
                        }
                    }
                }
            }
        }
        // Columns
        for (c in 0 until 9) {
            val pairs = (0 until 9).filter { candidates[it][c].size == 2 }
            for (i in pairs.indices) {
                for (j in i + 1 until pairs.size) {
                    val r1 = pairs[i]
                    val r2 = pairs[j]
                    if (candidates[r1][c] == candidates[r2][c]) {
                        val vals = candidates[r1][c]
                        for (r in 0 until 9) {
                            if (r != r1 && r != r2) {
                                if (candidates[r][c].removeAll(vals)) changed = true
                            }
                        }
                    }
                }
            }
        }
        // Boxes
        for (b in 0 until 9) {
            val startR = (b / 3) * 3
            val startC = (b % 3) * 3
            val cellCoords = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    cellCoords.add(startR + i to startC + j)
                }
            }
            val pairs = cellCoords.filter { candidates[it.first][it.second].size == 2 }
            for (i in pairs.indices) {
                for (j in i + 1 until pairs.size) {
                    val p1 = pairs[i]
                    val p2 = pairs[j]
                    if (candidates[p1.first][p1.second] == candidates[p2.first][p2.second]) {
                        val vals = candidates[p1.first][p1.second]
                        for (coord in cellCoords) {
                            if (coord != p1 && coord != p2) {
                                if (candidates[coord.first][coord.second].removeAll(vals)) changed = true
                            }
                        }
                    }
                }
            }
        }
        return changed
    }

    private fun applyXWing(): Boolean {
        var changed = false
        for (v in 1..9) {
            // Rows X-Wing
            val rowPositions = Array(9) { r -> (0 until 9).filter { c -> candidates[r][c].contains(v) } }
            for (r1 in 0 until 9) {
                if (rowPositions[r1].size == 2) {
                    for (r2 in r1 + 1 until 9) {
                        if (rowPositions[r2] == rowPositions[r1]) {
                            val c1 = rowPositions[r1][0]
                            val c2 = rowPositions[r1][1]
                            for (r in 0 until 9) {
                                if (r != r1 && r != r2) {
                                    if (candidates[r][c1].remove(v)) changed = true
                                    if (candidates[r][c2].remove(v)) changed = true
                                }
                            }
                        }
                    }
                }
            }
            // Columns X-Wing
            val colPositions = Array(9) { c -> (0 until 9).filter { r -> candidates[r][c].contains(v) } }
            for (c1 in 0 until 9) {
                if (colPositions[c1].size == 2) {
                    for (c2 in c1 + 1 until 9) {
                        if (colPositions[c2] == colPositions[c1]) {
                            val r1 = colPositions[c1][0]
                            val r2 = colPositions[c1][1]
                            for (c in 0 until 9) {
                                if (c != c1 && c != c2) {
                                    if (candidates[r1][c].remove(v)) changed = true
                                    if (candidates[r2][c].remove(v)) changed = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return changed
    }

    private fun setValue(r: Int, c: Int, v: Int) {
        grid[r][c] = v
        candidates[r][c].clear()
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
