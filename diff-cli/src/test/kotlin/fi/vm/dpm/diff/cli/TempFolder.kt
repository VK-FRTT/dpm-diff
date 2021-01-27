package fi.vm.dpm.diff.cli

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path

class TempFolder(discriminator: String) : Closeable {

    private val rootPath = Files.createTempDirectory(discriminator)

    override fun close() {
        Files
            .walk(rootPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    fun resolve(subPath: String): Path {
        return rootPath.resolve(subPath)
    }
}
