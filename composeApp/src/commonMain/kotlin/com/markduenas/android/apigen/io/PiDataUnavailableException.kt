package com.markduenas.android.apigen.io

/**
 * Thrown when the bundled pi digits can't be loaded or are corrupt. Callers should show an error
 * rather than substitute made-up digits.
 */
class PiDataUnavailableException(message: String, cause: Throwable? = null) : Exception(message, cause)

object PiDataValidation {
    /** "3." followed by one million digits. */
    const val EXPECTED_LENGTH = 1_000_002

    /** Returns a description of what's wrong with [content], or null if it is a complete pi file. */
    fun problemWith(content: String?): String? = when {
        content.isNullOrEmpty() -> "pi digits file is missing or empty"
        !content.startsWith("3.") -> "pi digits file does not start with \"3.\""
        content.length < EXPECTED_LENGTH -> "pi digits file is truncated (${content.length} of $EXPECTED_LENGTH characters)"
        content.drop(2).any { !it.isDigit() } -> "pi digits file contains non-digit characters"
        else -> null
    }
}
