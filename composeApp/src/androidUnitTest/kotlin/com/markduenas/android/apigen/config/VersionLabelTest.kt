package com.markduenas.android.apigen.config

import com.markduenas.android.apigen.BuildConfig as GeneratedBuildConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionLabelTest {
    @Test
    fun androidVersionLabelIsVersionNameAndCode() {
        assertEquals(
            "${GeneratedBuildConfig.VERSION_NAME} (${GeneratedBuildConfig.VERSION_CODE})",
            BuildConfig.versionLabel
        )
    }

    @Test
    fun androidVersionLabelIsNotHardCoded() {
        val label = BuildConfig.versionLabel
        assertTrue(Regex("""^\d+(\.\d+)*(-[A-Za-z]+)? \(\d+\)$""").matches(label), "unexpected label: $label")
        assertTrue(label != "2.5", "About must not show the old hard-coded 2.5")
    }
}
