package fi.vm.dpm.diff.cli

import java.io.PrintWriter

object DiffCliVersion {

    fun printVersion(outWriter: PrintWriter) {
        val version = resolveVersion()

        outWriter.println()
        outWriter.println("Build time:   ${version.buildTime}")
        outWriter.println("Revision:     ${version.buildRevision}")
        outWriter.println("Origin URL:   ${version.originUrl}")
        outWriter.println()
    }

    fun resolveVersion(): VersionInfo {
        val classPath = javaClass.getResource(javaClass.simpleName + ".class").toString()

        return if (classPath.startsWith("jar:file:")) {
            val stream = VersionInfoReader.manifestStream()
            stream.use {
                VersionInfoReader.versionFromManifestStream(it)
            }
        } else {
            VersionInfo(
                buildTime = "dev-build-time",
                buildRevision = "dev-revision",
                originUrl = "dev-origin"
            )
        }
    }
}
