package fi.vm.dpm.diff.cli

import java.io.PrintWriter
import java.net.URL
import java.util.jar.Manifest

object DiffCliVersion {

    data class VersionInfo(
        val buildTime: String,
        val buildRevision: String,
        val originUrl: String
    )

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

            val manifestPath = "${classPath.substringBefore("!")}!/META-INF/MANIFEST.MF"

            URL(manifestPath).openStream().use {
                val manifestAttributes = Manifest(it).mainAttributes

                VersionInfo(
                    buildTime = manifestAttributes.getValue("Build-Timestamp"),
                    buildRevision = manifestAttributes.getValue("Build-Revision"),
                    originUrl = manifestAttributes.getValue("Build-OriginUrl")
                )
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
