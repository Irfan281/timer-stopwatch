package com.example.timerstopwatch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_prefs")

class TimerDataStore(private val context: Context) {
    
    companion object {
        val TIMER_REMAINING_MS = longPreferencesKey("timer_remaining_ms")
        val TIMER_IS_RUNNING = longPreferencesKey("timer_is_running")
        val TIMER_END_TIME = longPreferencesKey("timer_end_time")
        val STOPWATCH_ELAPSED_MS = longPreferencesKey("stopwatch_elapsed_ms")
        val STOPWATCH_IS_RUNNING = longPreferencesKey("stopwatch_is_running")
        val STOPWATCH_START_TIME = longPreferencesKey("stopwatch_start_time")
    }
    
    // Timer State
    val timerRemainingMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[TIMER_REMAINING_MS] ?: 300000L // Default 5 minutes
    }
    
    val timerIsRunning: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TIMER_IS_RUNNING] == 1L
    }
    
    val timerEndTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[TIMER_END_TIME] ?: 0L
    }
    
    // Stopwatch State
    val stopwatchElapsedMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[STOPWATCH_ELAPSED_MS] ?: 0L
    }
    
    val stopwatchIsRunning: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[STOPWATCH_IS_RUNNING] == 1L
    }
    
    val stopwatchStartTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[STOPWATCH_START_TIME] ?: 0L
    }
    
    // Save Timer State
    suspend fun saveTimerState(remainingMs: Long, isRunning: Boolean, endTime: Long) {
        context.dataStore.edit { prefs ->
            prefs[TIMER_REMAINING_MS] = remainingMs
            prefs[TIMER_IS_RUNNING] = if (isRunning) 1L else 0L
            prefs[TIMER_END_TIME] = endTime
        }
    }
    
    // Save Stopwatch State
    suspend fun saveStopwatchState(elapsedMs: Long, isRunning: Boolean, startTime: Long) {
        context.dataStore.edit { prefs ->
            prefs[STOPWATCH_ELAPSED_MS] = elapsedMs
            prefs[STOPWATCH_IS_RUNNING] = if (isRunning) 1L else 0L
            prefs[STOPWATCH_START_TIME] = startTime
        }
    }
    
    // Clear Timer State
    suspend fun clearTimerState() {
        context.dataStore.edit { prefs ->
            prefs[TIMER_REMAINING_MS] = 300000L
            prefs[TIMER_IS_RUNNING] = 0L
            prefs[TIMER_END_TIME] = 0L
        }
    }
    
    // Clear Stopwatch State
    suspend fun clearStopwatchState() {
        context.dataStore.edit { prefs ->
            prefs[STOPWATCH_ELAPSED_MS] = 0L
            prefs[STOPWATCH_IS_RUNNING] = 0L
            prefs[STOPWATCH_START_TIME] = 0L
        }
    }
}
