package fi.vm.dpm.diff.cli

import java.nio.charset.Charset
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val status = DiffCli(
        System.out,
        System.err,
        Charset.defaultCharset(),
        DefinedOptions()
    ).use { cli ->
        cli.execute(args)
    }

    exitProcess(status)
}
