package fi.vm.dpm.diff.cli

class HaltException : RuntimeException()
class FailException(message: String) : RuntimeException(message)

fun throwHalt(): Nothing {
    throw HaltException()
}

fun throwFail(errorMessage: String): Nothing {
    throw FailException(errorMessage)
}

fun thisShouldNeverHappen(message: String): Nothing {
    throw IllegalStateException(message)
}
