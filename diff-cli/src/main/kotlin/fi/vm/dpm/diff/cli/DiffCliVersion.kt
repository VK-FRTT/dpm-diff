package fi.vm.dpm.diff.cli

import java.io.PrintWriter
import java.net.URL
import java.util.jar.Manifest

object DiffCliVersion {

    data class VersionInfo(
        val version: String,
        val buildTime: String,
        val buildRevision: String
    )

    fun printVersion(outWriter: PrintWriter) {
        val version = resolveVersion()

        outWriter.println()
        outWriter.println("Version:      ${version.version}")
        outWriter.println("Build time:   ${version.buildTime}")
        outWriter.println("Revision:     ${version.buildRevision}")
        outWriter.println()
    }

    private fun resolveVersion(): VersionInfo {
        val classPath = javaClass.getResource(javaClass.simpleName + ".class").toString()

        return if (classPath.startsWith("jar:file:")) {

            val manifestPath = "${classPath.substringBefore("!")}!/META-INF/MANIFEST.MF"

            URL(manifestPath).openStream().use {
                val manifestAttributes = Manifest(it).mainAttributes

                VersionInfo(
                    version = manifestAttributes.getValue("Implementation-Version"),
                    buildTime = manifestAttributes.getValue("Build-Timestamp"),
                    buildRevision = manifestAttributes.getValue("Build-Revision")
                )
            }
        } else {
            VersionInfo(
                version = "0.0.0-DEV",
                buildTime = "-",
                buildRevision = "-"
            )
        }
    }
}
