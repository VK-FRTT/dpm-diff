package fi.vm.dpm.diff.cli.unit

import fi.vm.dpm.diff.cli.VersionInfoReader
import java.io.FileNotFoundException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class VersionInfoReaderTest {

    @Test
    fun `manifestStream should fail as the real JAR manifest is not in place during tests`() {
        val thrown = Assertions.catchThrowable {
            VersionInfoReader.manifestStream()
        }

        assertThat(thrown).isInstanceOf(FileNotFoundException::class.java)
        assertThat(thrown).hasMessageEndingWith("!/META-INF/MANIFEST.MF (No such file or directory)")
    }

    @Test
    fun `versionFromManifestStream should read content from manifest fixture`() {
        val stream = javaClass.getResourceAsStream("/manifest_fixture/MANIFEST.MF")

        val versionInfo = stream.use {
            VersionInfoReader.versionFromManifestStream(stream)
        }

        assertThat(versionInfo.buildTime).isEqualTo("2021-01-20T10:20:30.400+02:00")
        assertThat(versionInfo.buildRevision).isEqualTo("abcd1234")
        assertThat(versionInfo.originUrl).isEqualTo("https://github.com/VK-FRTT/dpm-diff.git")
    }
}
