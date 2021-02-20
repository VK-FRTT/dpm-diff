package fi.vm.dpm.diff.cli

import java.io.InputStream
import java.net.URL
import java.util.jar.Manifest

object VersionInfoReader {

    fun manifestStream(): InputStream {
        val classPath = javaClass.getResource(javaClass.simpleName + ".class").toString()
        val manifestPath = "${classPath.substringBefore("!")}!/META-INF/MANIFEST.MF"

        return URL(manifestPath).openStream()
    }

    fun versionFromManifestStream(stream: InputStream): VersionInfo {

        val manifestAttributes = Manifest(stream).mainAttributes

        return VersionInfo(
            buildTime = manifestAttributes.getValue("Build-Timestamp"),
            buildRevision = manifestAttributes.getValue("Build-Revision"),
            originUrl = manifestAttributes.getValue("Build-OriginUrl")
        )
    }
}
