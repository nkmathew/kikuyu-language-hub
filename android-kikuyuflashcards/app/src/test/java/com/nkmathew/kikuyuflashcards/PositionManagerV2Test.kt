package com.nkmathew.kikuyuflashcards

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class PositionManagerV2Test {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    private lateinit var positionManagerV2: PositionManagerV2

    @Before
    fun setup() {
        // Mock SharedPreferences
        whenever(context.getSharedPreferences(any(), eq(Context.MODE_PRIVATE)))
            .thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putInt(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putLong(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putString(any(), any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.apply()).then {}

        // Create PositionManagerV2 instance
        positionManagerV2 = PositionManagerV2(context)
    }

    @Test
    fun testSavePositionWithKey() {
        // Set up test data
        val position = 5
        val key = "vocabulary:beginner"
        val totalCards = 10

        // Call the method under test
        positionManagerV2.savePositionWithKey(position, key, totalCards)

        // Verify SharedPreferences interactions
        verify(sharedPreferencesEditor, atLeastOnce()).putInt(matches("pos_key_$key"), eq(position))
        verify(sharedPreferencesEditor, atLeastOnce()).putInt(matches("total_key_$key"), eq(totalCards))
        verify(sharedPreferencesEditor, atLeastOnce()).putInt(matches("progress_key_$key"), eq(60)) // (5+1)*100/10 = 60%
        verify(sharedPreferencesEditor, atLeastOnce()).apply()
    }

    @Test
    fun testGetLastPositionWithKey() {
        // Set up test data
        val key = "vocabulary:beginner"
        val expectedPosition = 5

        // Mock SharedPreferences.getInt()
        whenever(sharedPreferences.getInt(eq("pos_key_$key"), eq(0))).thenReturn(expectedPosition)

        // Call the method under test
        val position = positionManagerV2.getLastPositionWithKey(key)

        // Verify the result
        assertEquals(expectedPosition, position)
    }

    @Test
    fun testGetTotalCountWithKey() {
        // Set up test data
        val key = "vocabulary:beginner"
        val expectedCount = 10

        // Mock SharedPreferences.getInt()
        whenever(sharedPreferences.getInt(eq("total_key_$key"), eq(0))).thenReturn(expectedCount)

        // Call the method under test
        val count = positionManagerV2.getTotalCountWithKey(key)

        // Verify the result
        assertEquals(expectedCount, count)
    }

    @Test
    fun testGetProgressWithKey() {
        // Set up test data
        val key = "vocabulary:beginner"
        val expectedProgress = 50

        // Mock SharedPreferences.getInt()
        whenever(sharedPreferences.getInt(eq("progress_key_$key"), eq(0))).thenReturn(expectedProgress)

        // Call the method under test
        val progress = positionManagerV2.getProgressWithKey(key)

        // Verify the result
        assertEquals(expectedProgress, progress)
    }

    @Test
    fun testGetKeyProgress() {
        // Set up test data
        val key = "vocabulary:beginner"
        val expectedPosition = 5
        val expectedCount = 10
        val expectedProgress = 60

        // Mock SharedPreferences.getInt()
        whenever(sharedPreferences.getInt(eq("pos_key_$key"), eq(0))).thenReturn(expectedPosition)
        whenever(sharedPreferences.getInt(eq("total_key_$key"), eq(0))).thenReturn(expectedCount)
        whenever(sharedPreferences.getInt(eq("progress_key_$key"), eq(0))).thenReturn(expectedProgress)

        // Call the method under test
        val progress = positionManagerV2.getKeyProgress(key)

        // Verify the result
        assertEquals(key, progress.key)
        assertEquals(expectedPosition, progress.lastPosition)
        assertEquals(expectedCount, progress.totalCards)
        assertEquals(expectedProgress, progress.progressPercent)
        assertEquals(true, progress.hasProgress)
    }

    @Test
    fun testParseKey() {
        // Test category:difficulty format
        val key1 = "vocabulary:beginner"
        val (category1, difficulty1) = positionManagerV2.getKeyProgress(key1).parseKey()
        assertEquals("vocabulary", category1)
        assertEquals("beginner", difficulty1)

        // Test category only format
        val key2 = "vocabulary:"
        val (category2, difficulty2) = positionManagerV2.getKeyProgress(key2).parseKey()
        assertEquals("vocabulary", category2)
        assertEquals("all", difficulty2)

        // Test empty format
        val key3 = ""
        val (category3, difficulty3) = positionManagerV2.getKeyProgress(key3).parseKey()
        assertEquals("all", category3)
        assertEquals("all", difficulty3)
    }

    @Test
    fun testGetAllKeys() {
        // Mock SharedPreferences.all
        val prefsMap = mapOf(
            "pos_key_vocabulary:beginner" to 1,
            "pos_key_phrases:intermediate" to 2,
            "other_key" to 3
        )
        whenever(sharedPreferences.all).thenReturn(prefsMap)

        // Call the method under test
        val keys = positionManagerV2.getAllKeys()

        // Verify the result
        assertEquals(2, keys.size)
        assert(keys.contains("vocabulary:beginner"))
        assert(keys.contains("phrases:intermediate"))
    }
}