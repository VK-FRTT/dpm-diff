package fi.vm.dpm.diff.repgen.ext.kotlin

fun Iterable<String>.toQuotedAndCommaSeparatedString(): String {
    return map { "'$it'" }
        .joinToString()
}
