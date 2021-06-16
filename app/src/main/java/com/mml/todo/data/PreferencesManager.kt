package com.mml.todo.data

import android.content.Context
import android.util.Log
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_pref")

enum class SortOrder {
    BY_NAME, BY_DATE
}

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

private const val TAG = "PreferencesManager"

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore


    val preferencesFlow = dataStore.data
        .catch { exception ->
            Log.e(TAG, "error reading preferences", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->


            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )

            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false

            FilterPreferences(
                sortOrder, hideCompleted
            )

        }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit {
            preferences -> preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompelted(hideCompleted: Boolean){
        dataStore.edit {
                preferences -> preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
    }
}