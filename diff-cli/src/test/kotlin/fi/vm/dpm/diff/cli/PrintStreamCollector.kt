package fi.vm.dpm.diff.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

class PrintStreamCollector(val charset: Charset) {
    private val baos = ByteArrayOutputStream()
    private val ps = PrintStream(baos, true, charset.name())

    fun printStream(): PrintStream = ps

    fun grabText(): String {
        ps.close()
        return String(baos.toByteArray(), charset)
    }
}
