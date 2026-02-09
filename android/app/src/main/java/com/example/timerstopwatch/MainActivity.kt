package com.example.timerstopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.timerstopwatch.data.TimerDataStore
import com.example.timerstopwatch.ui.screens.StopwatchScreen
import com.example.timerstopwatch.ui.screens.TimerScreen
import com.example.timerstopwatch.ui.theme.TimerStopwatchTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var dataStore: TimerDataStore
    
    // Stopwatch state
    private var stopwatchElapsedMs by mutableLongStateOf(0L)
    private var stopwatchIsRunning by mutableStateOf(false)
    private var stopwatchStartTime by mutableLongStateOf(0L)
    private var laps = mutableStateListOf<Pair<Int, String>>()
    private var lapCounter by mutableIntStateOf(0)
    
    // Timer state
    private var timerRemainingMs by mutableLongStateOf(300000L) // 5 minutes default
    private var timerIsRunning by mutableStateOf(false)
    private var timerEndTime by mutableLongStateOf(0L)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        dataStore = TimerDataStore(this)
        
        // Load saved states
        lifecycleScope.launch {
            dataStore.stopwatchElapsedMs.collect { stopwatchElapsedMs = it }
        }
        lifecycleScope.launch {
            dataStore.stopwatchIsRunning.collect { savedRunning ->
                if (savedRunning && stopwatchElapsedMs > 0) {
                    stopwatchIsRunning = true
                }
            }
        }
        lifecycleScope.launch {
            dataStore.timerRemainingMs.collect { timerRemainingMs = it }
        }
        lifecycleScope.launch {
            dataStore.timerIsRunning.collect { savedRunning ->
                if (savedRunning) {
                    lifecycleScope.launch {
                        dataStore.timerEndTime.collect { savedEndTime ->
                            if (savedEndTime > System.currentTimeMillis()) {
                                timerIsRunning = true
                                timerEndTime = savedEndTime
                            } else {
                                // Timer expired while app was closed
                                timerRemainingMs = 0L
                                lifecycleScope.launch {
                                    dataStore.saveTimerState(0L, false, 0L)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        setContent {
            TimerStopwatchTheme {
                MainScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        var selectedTab by remember { mutableIntStateOf(0) }
        
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.WatchLater, contentDescription = "Stopwatch") },
                        label = { Text("Stopwatch") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Timer, contentDescription = "Timer") },
                        label = { Text("Timer") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> StopwatchScreen(
                    elapsedMs = stopwatchElapsedMs,
                    isRunning = stopwatchIsRunning,
                    onStart = {
                        stopwatchIsRunning = true
                        stopwatchStartTime = System.currentTimeMillis() - stopwatchElapsedMs
                    },
                    onPause = {
                        stopwatchIsRunning = false
                        lifecycleScope.launch {
                            dataStore.saveStopwatchState(stopwatchElapsedMs, false, stopwatchStartTime)
                        }
                    },
                    onReset = {
                        stopwatchIsRunning = false
                        stopwatchElapsedMs = 0L
                        lapCounter = 0
                        laps.clear()
                        lifecycleScope.launch {
                            dataStore.clearStopwatchState()
                        }
                    },
                    onSaveState = { elapsed, running, start ->
                        stopwatchElapsedMs = elapsed
                        lifecycleScope.launch {
                            dataStore.saveStopwatchState(elapsed, running, start)
                        }
                    },
                    laps = laps,
                    onAddLap = {
                        lapCounter++
                        val timeStr = formatStopwatchTime(stopwatchElapsedMs)
                        laps.add(Pair(lapCounter, timeStr))
                    },
                    onClearLaps = {
                        laps.clear()
                        lapCounter = 0
                    }
                )
                1 -> TimerScreen(
                    initialRemainingMs = timerRemainingMs,
                    isRunning = timerIsRunning,
                    endTime = timerEndTime,
                    onStart = { endTime ->
                        timerIsRunning = true
                        timerEndTime = endTime
                    },
                    onPause = { remaining ->
                        timerIsRunning = false
                        timerRemainingMs = remaining
                        lifecycleScope.launch {
                            dataStore.saveTimerState(remaining, false, 0L)
                        }
                    },
                    onReset = {
                        timerIsRunning = false
                        lifecycleScope.launch {
                            dataStore.clearTimerState()
                        }
                    },
                    onSaveState = { remaining, running, end ->
                        timerRemainingMs = remaining
                        lifecycleScope.launch {
                            dataStore.saveTimerState(remaining, running, end)
                        }
                    }
                )
            }
        }
    }
    
    private fun formatStopwatchTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = (ms % 1000) / 10
        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis)
    }
}
