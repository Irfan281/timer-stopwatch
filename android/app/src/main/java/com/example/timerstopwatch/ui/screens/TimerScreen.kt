package com.example.timerstopwatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    initialRemainingMs: Long,
    isRunning: Boolean,
    endTime: Long,
    onStart: (Long) -> Unit,
    onPause: (Long) -> Unit,
    onReset: () -> Unit,
    onSaveState: (Long, Boolean, Long) -> Unit
) {
    var hours by remember { mutableIntStateOf((initialRemainingMs / 3600000).toInt()) }
    var minutes by remember { mutableIntStateOf(((initialRemainingMs % 3600000) / 60000).toInt()) }
    var seconds by remember { mutableIntStateOf(((initialRemainingMs % 60000) / 1000).toInt()) }
    
    var currentRemaining by remember { mutableLongStateOf(initialRemainingMs) }
    var localEndTime by remember { mutableLongStateOf(endTime) }
    var isTimerFinished by remember { mutableStateOf(false) }
    
    // Calculate total milliseconds from inputs
    fun calculateTotalMs(): Long {
        return (hours * 3600L + minutes * 60L + seconds) * 1000L
    }
    
    // Update current remaining when inputs change (only when not running)
    LaunchedEffect(hours, minutes, seconds) {
        if (!isRunning) {
            currentRemaining = calculateTotalMs()
        }
    }
    
    // Timer countdown
    LaunchedEffect(isRunning, endTime) {
        if (isRunning && endTime > 0) {
            localEndTime = endTime
            isTimerFinished = false
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = localEndTime - now
                
                if (remaining <= 0) {
                    currentRemaining = 0
                    isTimerFinished = true
                    onSaveState(0, false, 0)
                    break
                }
                
                currentRemaining = remaining
                onSaveState(remaining, true, localEndTime)
                delay(10)
            }
        }
    }
    
    // Format time: HH:MM:SS.ms
    fun formatTime(ms: Long): String {
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        val s = (ms % 60000) / 1000
        val millis = (ms % 1000) / 10
        return String.format("%02d:%02d:%02d.%02d", h, m, s, millis)
    }
    
    // Play alarm when finished
    LaunchedEffect(isTimerFinished) {
        if (isTimerFinished) {
            // Could add sound here
            isTimerFinished = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time Input (only when not running)
            if (!isRunning && currentRemaining == calculateTotalMs()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Hours
                    TimeInputField(
                        value = hours,
                        onValueChange = { hours = it.coerceIn(0, 99) },
                        label = "Hours"
                    )
                    
                    // Minutes
                    TimeInputField(
                        value = minutes,
                        onValueChange = { minutes = it.coerceIn(0, 59) },
                        label = "Minutes"
                    )
                    
                    // Seconds
                    TimeInputField(
                        value = seconds,
                        onValueChange = { seconds = it.coerceIn(0, 59) },
                        label = "Seconds"
                    )
                }
            }
            
            // Time Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentRemaining <= 10000 && currentRemaining > 0 && isRunning)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Text(
                    text = formatTime(currentRemaining),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = if (currentRemaining <= 10000 && currentRemaining > 0 && isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        LocalContentColor.current,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
            
            // Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Start Button
                Button(
                    onClick = {
                        if (!isRunning) {
                            val totalMs = calculateTotalMs()
                            if (totalMs > 0) {
                                val newEndTime = System.currentTimeMillis() + totalMs
                                onStart(newEndTime)
                            }
                        }
                    },
                    enabled = !isRunning && currentRemaining > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    Spacer(Modifier.width(8.dp))
                    Text("START")
                }
                
                // Pause Button
                Button(
                    onClick = { onPause(currentRemaining) },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                    Spacer(Modifier.width(8.dp))
                    Text("PAUSE")
                }
                
                // Reset Button
                Button(
                    onClick = {
                        onReset()
                        currentRemaining = calculateTotalMs()
                        hours = (initialRemainingMs / 3600000).toInt()
                        minutes = ((initialRemainingMs % 3600000) / 60000).toInt()
                        seconds = ((initialRemainingMs % 60000) / 1000).toInt()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(Modifier.width(8.dp))
                    Text("RESET")
                }
            }
            
            // Quick Presets (when not running)
            if (!isRunning) {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "1m" to Pair(0, 1),
                        "5m" to Pair(0, 5),
                        "10m" to Pair(0, 10),
                        "15m" to Pair(0, 15),
                        "25m" to Pair(0, 25)
                    ).forEach { (label, time) ->
                        OutlinedButton(
                            onClick = {
                                hours = 0
                                minutes = time.second
                                seconds = 0
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeInputField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { 
                it.toIntOrNull()?.let { num -> onValueChange(num) }
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(100.dp),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
