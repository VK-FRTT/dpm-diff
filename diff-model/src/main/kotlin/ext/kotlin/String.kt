package ext.kotlin

fun String.trimLineStartsAndConsequentBlankLines(): String {
    return lines()
        .map { it.trim() }
        .filterNotByCurrentAndNext { current, next -> current.isBlank() && next?.isBlank() ?: true }
        .joinToString(separator = "\n")
}

private fun List<String>.filterNotByCurrentAndNext(predicate: (String, String?) -> Boolean): List<String> {
    val destination = mutableListOf<String>()

    for (index in 1 until size) {
        val currentElement = get(index)

        val nextElementIndex = index + 1
        val nextElement = if (nextElementIndex < size) {
            get(nextElementIndex)
        } else {
            null
        }

        if (!predicate(currentElement, nextElement)) destination.add(currentElement)
    }
    return destination
}

fun String.replaceCamelCase(replacement: String = " "): String {
    return CAMEL_CASE_SPLIT_PATTERN.replace(this, replacement)
}

private val CAMEL_CASE_SPLIT_PATTERN =
    """
    (?<=[A-Z])(?=[A-Z][a-z])
    |
    (?<=[^A-Z])(?=[A-Z])
    |
    (?<=[A-Za-z])(?=[^A-Za-z])
    """.trimIndent().toRegex(RegexOption.COMMENTS)
