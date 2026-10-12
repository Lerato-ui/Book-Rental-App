package com.example.libraryrental.logic

object InputValidator {

    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 40
    const val MAX_QUERY_LENGTH = 50

    private val whitespace = Regex("[\\s\\u00A0]+")

    fun sanitizeName(raw: String): String {
        return raw.replace(whitespace, " ")
            .filterNot { it.isISOControl() }
            .trim()
            .take(MAX_NAME_LENGTH)
            .trim()
    }

    fun isValidName(name: String): Boolean {
        return name.length >= MIN_NAME_LENGTH
    }

    fun sanitizeSearchQuery(raw: String): String {
        return raw.replace(whitespace, " ")
            .filterNot { it.isISOControl() }
            .trim()
            .take(MAX_QUERY_LENGTH)
    }

    fun escapeForLike(query: String): String {
        return query.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }
}
