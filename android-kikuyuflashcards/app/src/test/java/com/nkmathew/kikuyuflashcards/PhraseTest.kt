package com.nkmathew.kikuyuflashcards

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Phrase data class
 */
class PhraseTest {

    @Test
    fun `phrase creation with all parameters`() {
        val phrase = Phrase("Hello", "Úhoro", "greetings")
        
        assertEquals("Hello", phrase.english)
        assertEquals("Úhoro", phrase.kikuyu)
        assertEquals("greetings", phrase.category)
        assertEquals("Hello", phrase.text) // backward compatibility
    }

    @Test
    fun `phrase creation with default category`() {
        val phrase = Phrase("Hello", "Úhoro")
        
        assertEquals("Hello", phrase.english)
        assertEquals("Úhoro", phrase.kikuyu)
        assertEquals("general", phrase.category) // default category
        assertEquals("Hello", phrase.text)
    }

    @Test
    fun `phrase equality works correctly`() {
        val phrase1 = Phrase("Hello", "Úhoro", "greetings")
        val phrase2 = Phrase("Hello", "Úhoro", "greetings")
        val phrase3 = Phrase("Hello", "Úhoro", "basic_words")
        
        assertEquals(phrase1, phrase2)
        assertNotEquals(phrase1, phrase3)
    }

    @Test
    fun `phrase handles empty strings`() {
        val phrase = Phrase("", "", "")
        
        assertEquals("", phrase.english)
        assertEquals("", phrase.kikuyu)
        assertEquals("", phrase.category)
        assertEquals("", phrase.text)
    }

    @Test
    fun `phrase handles special characters`() {
        val phrase = Phrase("How are you?", "Úhana atía?", "greetings")
        
        assertEquals("How are you?", phrase.english)
        assertEquals("Úhana atía?", phrase.kikuyu)
        assertEquals("greetings", phrase.category)
    }

    @Test
    fun `phrase toString representation`() {
        val phrase = Phrase("Hello", "Úhoro", "greetings")
        val toString = phrase.toString()
        
        assertTrue(toString.contains("Hello"))
        assertTrue(toString.contains("Úhoro"))
        assertTrue(toString.contains("greetings"))
    }

    @Test
    fun `phrase copy works correctly`() {
        val phrase1 = Phrase("Hello", "Úhoro", "greetings")
        val phrase2 = phrase1.copy()
        val phrase3 = phrase1.copy(category = "basic_words")
        
        assertEquals(phrase1, phrase2)
        assertNotEquals(phrase1, phrase3)
        assertEquals("basic_words", phrase3.category)
    }
}