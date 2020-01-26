package ext.kotlin

fun String.trimLineStartsAndBlankLines(): String {
    return lines()
        .map { it.trim() }
        .filterNot { it.isBlank() }
        .joinToString(separator = "\n")
}
