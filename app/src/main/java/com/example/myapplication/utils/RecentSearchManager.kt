package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.myapplication.front_end.search.RecentSearchItem


class RecentSearchManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson() // Use Gson to serialize/deserialize the list

    companion object {
        private const val PREFS_NAME = "RecentSearchesPrefs"
        private const val KEY_RECENT_SEARCHES = "recent_searches"
        private const val MAX_RECENT_SEARCHES = 15 // Limit the number of saved searches
    }

    fun getRecentSearches(): List<RecentSearchItem> {
        val json = prefs.getString(KEY_RECENT_SEARCHES, null) ?: return emptyList()
        return try {
            // Define the type for Gson deserialization
            val type = object : TypeToken<List<RecentSearchItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            // Handle potential deserialization errors (e.g., data corruption)
            emptyList()
        }
    }

    /**
     * Adds a new search item to the top of the recent searches list.
     * Ensures uniqueness (based on type and label) and trims the list if it exceeds the max size.
     */
    fun addSearchItem(newItem: RecentSearchItem) {
        val currentSearches = getRecentSearches().toMutableList()

        // Remove any existing item with the same type and label to avoid duplicates
        // and move the new one to the top.
        currentSearches.removeAll { it.type == newItem.type && it.label.equals(newItem.label, ignoreCase = true) }

        // Add the new item to the beginning of the list
        currentSearches.add(0, newItem)

        // Trim the list if it exceeds the maximum allowed size
        val trimmedList = if (currentSearches.size > MAX_RECENT_SEARCHES) {
            currentSearches.subList(0, MAX_RECENT_SEARCHES)
        } else {
            currentSearches
        }

        saveRecentSearches(trimmedList)
    }

    /**
     * Removes a specific search item from the list.
     */
    fun removeSearchItem(itemToRemove: RecentSearchItem) {
        val currentSearches = getRecentSearches().toMutableList()
        val removed = currentSearches.removeAll { it.type == itemToRemove.type && it.label.equals(itemToRemove.label, ignoreCase = true) }
        if (removed) {
            saveRecentSearches(currentSearches)
        }
    }

    /**
     * Clears all recent search items.
     */
    fun clearRecentSearches() {
        saveRecentSearches(emptyList())
    }

    /**
     * Saves the list of recent searches to SharedPreferences.
     */
    private fun saveRecentSearches(searches: List<RecentSearchItem>) {
        val json = gson.toJson(searches)
        prefs.edit {
            putString(KEY_RECENT_SEARCHES, json)
        }
    }
}