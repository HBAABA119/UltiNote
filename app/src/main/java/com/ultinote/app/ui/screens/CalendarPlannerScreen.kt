package com.ultinote.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultinote.app.data.local.KomorebiRepository
import com.ultinote.app.data.model.CoverStyle
import com.ultinote.app.data.model.NoteEntity
import com.ultinote.app.data.model.PaperTemplate
import com.ultinote.app.ui.components.LiquidGlassCard
import com.ultinote.app.ui.components.LiquidGlassPillButton
import com.ultinote.app.ui.components.NativeAnimatedDayCell
import com.ultinote.app.ui.components.NativeLargeTopBar
import com.ultinote.app.ui.components.nativePressable
import com.ultinote.app.ui.theme.LocalKomorebiPalette
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarPlannerScreen(
    repository: KomorebiRepository,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    showBackNavigation: Boolean = true
) {
    val palette = LocalKomorebiPalette.current
    val coroutineScope = rememberCoroutineScope()
    val allNotes by repository.allNotes.collectAsState(initial = emptyList())

    val calendar = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val currentMonthCalendar = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = (currentMonthCalendar.get(Calendar.DAY_OF_WEEK) - 1)

    val selectedDateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", displayedYear, displayedMonth + 1, selectedDay)

    val notesForSelectedDate = allNotes.filter { note ->
        note.linkedDate == selectedDateString ||
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(note.updatedAt) == selectedDateString
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NativeLargeTopBar(
            title = "Calendar",
            subtitle = "Plan study sessions by day",
            navigationIcon = if (showBackNavigation) {
                {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.colorScheme.onSurface
                        )
                    }
                }
            } else null
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 48.dp, end = 48.dp, bottom = 84.dp)
        ) {
            val isTabletWide = maxWidth >= 650.dp

            if (isTabletWide) {
                // TABLET SIDE-BY-SIDE LAYOUT: Calendar on Left, Planner Notes on Right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Calendar Card
                    LiquidGlassCard(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = palette.glassSurface,
                        elevation = 4.dp
                    ) {
                        CalendarGridContent(
                            currentMonthCalendar = currentMonthCalendar,
                            monthFormat = monthFormat,
                            displayedMonth = displayedMonth,
                            displayedYear = displayedYear,
                            daysInMonth = daysInMonth,
                            startDayOfWeek = startDayOfWeek,
                            selectedDay = selectedDay,
                            allNotes = allNotes,
                            onMonthPrev = {
                                if (displayedMonth == 0) {
                                    displayedMonth = 11
                                    displayedYear--
                                } else {
                                    displayedMonth--
                                }
                            },
                            onMonthNext = {
                                if (displayedMonth == 11) {
                                    displayedMonth = 0
                                    displayedYear++
                                } else {
                                    displayedMonth++
                                }
                            },
                            onSelectDay = { selectedDay = it }
                        )
                    }

                    // Right Column: Planner Notes for Selected Day
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Day $selectedDay Agenda",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.onSurface
                            )

                            LiquidGlassPillButton(
                                text = "New Day Planner",
                                icon = Icons.Default.Add,
                                isPrimary = true,
                                onClick = {
                                    coroutineScope.launch {
                                        val newNoteId = repository.createNote(
                                            title = "Planner • $selectedDateString",
                                            template = PaperTemplate.PLANNER_WEEKLY,
                                            coverStyle = CoverStyle.BOTANICAL,
                                            linkedDate = selectedDateString
                                        )
                                        onOpenNote(newNoteId)
                                    }
                                }
                            )
                        }

                        NotesListContent(
                            notes = notesForSelectedDate,
                            selectedDateString = selectedDateString,
                            onOpenNote = onOpenNote
                        )
                    }
                }
            } else {
                // PHONE / PORTRAIT VERTICAL LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = palette.glassSurface,
                        elevation = 3.dp
                    ) {
                        CalendarGridContent(
                            currentMonthCalendar = currentMonthCalendar,
                            monthFormat = monthFormat,
                            displayedMonth = displayedMonth,
                            displayedYear = displayedYear,
                            daysInMonth = daysInMonth,
                            startDayOfWeek = startDayOfWeek,
                            selectedDay = selectedDay,
                            allNotes = allNotes,
                            onMonthPrev = {
                                if (displayedMonth == 0) {
                                    displayedMonth = 11
                                    displayedYear--
                                } else {
                                    displayedMonth--
                                }
                            },
                            onMonthNext = {
                                if (displayedMonth == 11) {
                                    displayedMonth = 0
                                    displayedYear++
                                } else {
                                    displayedMonth++
                                }
                            },
                            onSelectDay = { selectedDay = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes for Day $selectedDay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.colorScheme.onSurface
                        )

                        LiquidGlassPillButton(
                            text = "New Planner",
                            icon = Icons.Default.Add,
                            isPrimary = true,
                            onClick = {
                                coroutineScope.launch {
                                    val newNoteId = repository.createNote(
                                        title = "Planner • $selectedDateString",
                                        template = PaperTemplate.PLANNER_WEEKLY,
                                        coverStyle = CoverStyle.BOTANICAL,
                                        linkedDate = selectedDateString
                                    )
                                    onOpenNote(newNoteId)
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NotesListContent(
                            notes = notesForSelectedDate,
                            selectedDateString = selectedDateString,
                            onOpenNote = onOpenNote
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGridContent(
    currentMonthCalendar: Calendar,
    monthFormat: SimpleDateFormat,
    displayedMonth: Int,
    displayedYear: Int,
    daysInMonth: Int,
    startDayOfWeek: Int,
    selectedDay: Int,
    allNotes: List<NoteEntity>,
    onMonthPrev: () -> Unit,
    onMonthNext: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    val palette = LocalKomorebiPalette.current

    Column(modifier = Modifier.padding(16.dp)) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthFormat.format(currentMonthCalendar.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.colorScheme.primary
            )

            Row {
                IconButton(onClick = onMonthPrev) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month", tint = palette.colorScheme.onSurface)
                }
                IconButton(onClick = onMonthNext) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month", tint = palette.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekdays Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = palette.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Days Grid
        val totalSlots = ((startDayOfWeek + daysInMonth + 6) / 7) * 7
        for (week in 0 until totalSlots / 7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in 0 until 7) {
                    val slotIndex = week * 7 + dayOfWeek
                    val dayNum = slotIndex - startDayOfWeek + 1

                    if (dayNum in 1..daysInMonth) {
                        val isSelected = dayNum == selectedDay
                        val slotDateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", displayedYear, displayedMonth + 1, dayNum)
                        val hasNotes = allNotes.any { it.linkedDate == slotDateStr }

                        NativeAnimatedDayCell(
                            dayNum = dayNum,
                            isSelected = isSelected,
                            hasNotes = hasNotes,
                            onClick = { onSelectDay(dayNum) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesListContent(
    notes: List<NoteEntity>,
    selectedDateString: String,
    onOpenNote: (String) -> Unit
) {
    val palette = LocalKomorebiPalette.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (notes.isEmpty()) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = palette.glassSurface,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = palette.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No notes scheduled",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap '+ New Day Planner' to log study goals or lectures for $selectedDateString.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(notes) { note ->
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nativePressable(scaleDown = 0.96f) { onOpenNote(note.id) },
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = palette.glassSurface,
                    elevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = palette.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${note.pageCount} pages • ${note.defaultTemplate.name.replace("_", " ")}",
                                fontSize = 12.sp,
                                color = palette.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
