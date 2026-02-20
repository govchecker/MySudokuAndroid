package com.example.mysudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysudoku.model.SudokuCell

@Composable
fun SudokuGameScreen(
    modifier: Modifier = Modifier,
    uiState: SudokuUiState,
    onCellClick: (Int, Int) -> Unit,
    onNumberInput: (Int) -> Unit,
    onToggleNoteMode: () -> Unit,
    onUndo: () -> Unit
) {
    val highlightValue = uiState.selectedNumber ?: uiState.selectedRow?.let { r ->
        uiState.selectedCol?.let { c ->
            uiState.grid[r * 9 + c].value
        }
    } ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SudokuGrid(
            grid = uiState.grid,
            selectedRow = uiState.selectedRow,
            selectedCol = uiState.selectedCol,
            highlightValue = highlightValue,
            onCellClick = onCellClick
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onUndo, enabled = uiState.history.isNotEmpty()) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo, // KORREKTE VERSION
                    contentDescription = "Rückgängig",
                    tint = if (uiState.history.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            
            FilterChip(
                selected = uiState.isNoteModeEnabled,
                onClick = onToggleNoteMode,
                label = { Text("Notizen") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        NumberInputPad(
            onNumberClick = onNumberInput,
            counts = uiState.numberCounts,
            isNoteMode = uiState.isNoteModeEnabled,
            selectedNumber = uiState.selectedNumber
        )
    }
}

@Composable
fun SudokuGrid(
    grid: List<SudokuCell>,
    selectedRow: Int?,
    selectedCol: Int?,
    highlightValue: Int,
    onCellClick: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, Color.Black)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) {
            items(grid) { cell ->
                val isSelected = cell.row == selectedRow && cell.col == selectedCol
                val isHighlighted = highlightValue != 0 && cell.value == highlightValue

                SudokuCellView(
                    cell = cell,
                    isSelected = isSelected,
                    isHighlighted = isHighlighted,
                    onClick = { onCellClick(cell.row, cell.col) }
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            for (i in 1 until 3) {
                val x = i * (size.width / 3f)
                drawLine(Color.Black, Offset(x, 0f), Offset(x, size.height), strokeWidth)
                val y = i * (size.height / 3f)
                drawLine(Color.Black, Offset(0f, y), Offset(size.width, y), strokeWidth)
            }
        }
    }
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    isSelected: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFFBBDEFB)
        isHighlighted -> Color(0xFFE3F2FD)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .border(0.5.dp, Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                fontSize = 22.sp,
                fontWeight = if (cell.isFixed) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    cell.isError -> Color.Red
                    cell.isFixed -> Color.Black
                    else -> Color(0xFF1976D2)
                }
            )
        } else if (cell.notes.isNotEmpty()) {
            NoteGrid(notes = cell.notes)
        }
    }
}

@Composable
fun NoteGrid(notes: Set<Int>) {
    Column(modifier = Modifier.fillMaxSize().padding(1.dp)) {
        for (row in 0 until 3) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 3) {
                    val num = row * 3 + (col + 1)
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberInputPad(
    onNumberClick: (Int) -> Unit,
    counts: Map<Int, Int>,
    isNoteMode: Boolean,
    selectedNumber: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 1..9) {
            val count = counts[i] ?: 0
            val remaining = 9 - count
            val isDone = remaining <= 0
            val isSelected = selectedNumber == i

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isDone) "✓" else remaining.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Color(0xFF4CAF50) else Color.Gray
                )

                Button(
                    onClick = { onNumberClick(i) },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFFF9800) 
                                        else if (isNoteMode) Color(0xFFFFB74D) 
                                        else if (isDone) Color.LightGray 
                                        else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isDone || isNoteMode
                ) {
                    Text(text = i.toString(), fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
