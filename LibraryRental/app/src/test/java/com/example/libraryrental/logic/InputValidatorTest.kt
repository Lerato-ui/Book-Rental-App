package com.example.libraryrental.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun sanitizeName_trimsAndCollapsesWhitespace() {
        assertEquals("Sam Smith", InputValidator.sanitizeName("  Sam    Smith  "))
        assertEquals("Sam Smith", InputValidator.sanitizeName("Sam\t\nSmith"))
    }

    @Test
    fun sanitizeName_removesControlCharacters() {
        assertEquals("Sam", InputValidator.sanitizeName("Sa\u0007m"))
    }

    @Test
    fun sanitizeName_capsLength() {
        val result = InputValidator.sanitizeName("a".repeat(100))
        assertEquals(InputValidator.MAX_NAME_LENGTH, result.length)
    }

    @Test
    fun isValidName_requiresMinimumLength() {
        assertFalse(InputValidator.isValidName(""))
        assertFalse(InputValidator.isValidName("A"))
        assertTrue(InputValidator.isValidName("Al"))
    }

    @Test
    fun blankNameIsInvalidAfterSanitizing() {
        assertFalse(InputValidator.isValidName(InputValidator.sanitizeName("     ")))
    }

    @Test
    fun sanitizeSearchQuery_trimsAndCaps() {
        assertEquals("dune", InputValidator.sanitizeSearchQuery("  dune "))
        assertEquals(
            InputValidator.MAX_QUERY_LENGTH,
            InputValidator.sanitizeSearchQuery("x".repeat(200)).length
        )
    }

    @Test
    fun escapeForLike_escapesWildcardsAndBackslash() {
        assertEquals("50\\%\\_off\\\\", InputValidator.escapeForLike("50%_off\\"))
        assertEquals("plain", InputValidator.escapeForLike("plain"))
    }
}
