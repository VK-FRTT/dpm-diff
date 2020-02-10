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
