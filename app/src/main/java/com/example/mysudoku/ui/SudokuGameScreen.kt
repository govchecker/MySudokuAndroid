package com.example.mysudoku.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mysudoku.model.SudokuCell
import com.example.mysudoku.model.SudokuHint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuGameScreen(
    modifier: Modifier = Modifier,
    uiState: SudokuUiState,
    onCellClick: (Int, Int) -> Unit,
    onNumberInput: (Int) -> Unit,
    onToggleNoteMode: () -> Unit,
    onUndo: () -> Unit,
    onNewGame: (Difficulty) -> Unit,
    onAutoFillNotes: () -> Unit,
    onShowDifficultyDialog: () -> Unit,
    onDismissDialog: () -> Unit,
    onRequestHint: () -> Unit,
    onClearHint: () -> Unit,
    onAnimationFinished: () -> Unit
) {
    val highlightValue = uiState.selectedNumber ?: uiState.selectedRow?.let { r ->
        uiState.selectedCol?.let { c ->
            val index = r * 9 + c
            if (index in uiState.grid.indices) uiState.grid[index].value else 0
        }
    } ?: 0

    // Timer für die Block-Animationen (2x Blinken dauert ca. 1 Sekunde)
    LaunchedEffect(uiState.animationEvent) {
        if (uiState.animationEvent != null) {
            delay(1100)
            onAnimationFinished()
        }
    }

    if (uiState.isGameWon) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Glückwunsch!") },
            text = { Text("Du hast das Sudoku in ${formatTime(uiState.timerSeconds)} gelöst!") },
            confirmButton = {
                Button(onClick = onShowDifficultyDialog) {
                    Text("Neues Spiel")
                }
            }
        )
    }

    if (uiState.currentHint != null) {
        AlertDialog(
            onDismissRequest = onClearHint,
            title = { Text("Hinweis") },
            text = { Text(uiState.currentHint.message) },
            confirmButton = {
                TextButton(onClick = onClearHint) {
                    Text("Verstanden")
                }
            }
        )
    }

    if (uiState.showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Schwierigkeitsgrad wählen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { difficulty ->
                        val isSelected = difficulty == uiState.difficulty
                        Button(
                            onClick = { onNewGame(difficulty) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (isSelected) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(difficulty.name)
                                if (isSelected) {
                                    Text("✓", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zeit: ${formatTime(uiState.timerSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = uiState.difficulty.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (uiState.grid.isNotEmpty()) {
            SudokuGrid(
                grid = uiState.grid,
                selectedRow = uiState.selectedRow,
                selectedCol = uiState.selectedCol,
                highlightValue = highlightValue,
                hint = uiState.currentHint,
                animationEvent = uiState.animationEvent,
                onCellClick = onCellClick
            )
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SudokuTooltip(text = "Rückgängig") {
                    IconButton(onClick = onUndo, enabled = uiState.history.isNotEmpty()) {
                        Icon(Icons.AutoMirrored.Filled.Undo, "Rückgängig", tint = if (uiState.history.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                SudokuTooltip(text = "Tipp erhalten") {
                    IconButton(onClick = onRequestHint) {
                        Icon(Icons.Default.Lightbulb, "Hinweis", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                SudokuTooltip(text = "Neues Spiel") {
                    IconButton(onClick = onShowDifficultyDialog) {
                        Icon(Icons.Default.Refresh, "Neues Spiel", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                SudokuTooltip(text = "Alle Hinweise automatisch füllen") {
                    IconButton(onClick = onAutoFillNotes) {
                        Icon(Icons.Default.ContentPasteSearch, "Hinweise autom. füllen", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            FilterChip(
                selected = uiState.isNoteModeEnabled,
                onClick = onToggleNoteMode,
                label = { Text("Notizen") },
                leadingIcon = { Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp)) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuTooltip(text: String, content: @Composable () -> Unit) {
    val tooltipState = rememberTooltipState(isPersistent = false)
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text) } },
        state = tooltipState
    ) {
        Box(modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Enter) { scope.launch { tooltipState.show() } }
                    if (event.type == PointerEventType.Exit) { tooltipState.dismiss() }
                }
            }
        }) { content() }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

@Composable
fun SudokuGrid(
    grid: List<SudokuCell>,
    selectedRow: Int?,
    selectedCol: Int?,
    highlightValue: Int,
    hint: SudokuHint?,
    animationEvent: AnimationEvent?,
    onCellClick: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        ) {
            LazyVerticalGrid(columns = GridCells.Fixed(9), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) {
                items(grid) { cell ->
                    val isSelected = cell.row == selectedRow && cell.col == selectedCol
                    val isHighlighted = highlightValue != 0 && cell.value == highlightValue
                    val isHinted = hint?.affectedCells?.contains(cell.row to cell.col) == true
                    
                    val isAnimating = animationEvent?.let { event ->
                        event.rows.contains(cell.row) || 
                        event.cols.contains(cell.col) || 
                        event.boxes.contains(cell.boxIndex)
                    } ?: false

                    SudokuCellView(
                        cell = cell,
                        isSelected = isSelected,
                        isHighlighted = isHighlighted,
                        isHinted = isHinted,
                        isAnimating = isAnimating,
                        onClick = { onCellClick(cell.row, cell.col) }
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val thickStrokeWidth = 4.dp.toPx()
                val color = Color.Black.copy(alpha = 0.7f)
                for (i in 1 until 3) {
                    val x = i * (size.width / 3f)
                    drawLine(color, Offset(x, 0f), Offset(x, size.height), thickStrokeWidth)
                    val y = i * (size.height / 3f)
                    drawLine(color, Offset(0f, y), Offset(size.width, y), thickStrokeWidth)
                }
            }
        }
    }
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    isSelected: Boolean,
    isHighlighted: Boolean,
    isHinted: Boolean,
    isAnimating: Boolean,
    onClick: () -> Unit
) {
    val animColor by animateColorAsState(
        targetValue = if (isAnimating) Color(0xFFFFD700).copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = if (isAnimating) {
            repeatable(
                iterations = 4,
                animation = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 200)
        },
        label = "cellAnimation"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                when {
                    isAnimating -> animColor
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    isHinted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                fontSize = 22.sp,
                fontWeight = if (cell.isFixed) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    cell.isError || cell.isWrong -> MaterialTheme.colorScheme.error
                    cell.isFixed -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        } else if (cell.notes.isNotEmpty()) {
            NoteGrid(notes = cell.notes)
        }
    }
}

@Composable
fun NoteGrid(notes: Set<Int>) {
    Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        for (row in 0 until 3) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 3) {
                    val num = row * 3 + (col + 1)
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (notes.contains(num)) {
                            Text(
                                text = num.toString(),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 9.sp
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
            val isDone = count >= 9
            val isSelected = selectedNumber == i

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isDone) "✓" else (9 - count).toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                ElevatedButton(
                    onClick = { onNumberClick(i) },
                    modifier = Modifier.aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when {
                            isSelected && !isNoteMode -> MaterialTheme.colorScheme.primary
                            isSelected && isNoteMode -> MaterialTheme.colorScheme.secondary
                            isNoteMode -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        },
                        contentColor = when {
                            isSelected && !isNoteMode -> MaterialTheme.colorScheme.onPrimary
                            isSelected && isNoteMode -> MaterialTheme.colorScheme.onSecondary
                            isNoteMode -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isDone || isNoteMode,
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
                ) {
                    Text(text = i.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
