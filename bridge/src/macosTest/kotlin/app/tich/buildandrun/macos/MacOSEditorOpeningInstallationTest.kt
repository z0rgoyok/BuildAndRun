package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.context.editors.model.Editor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacOSEditorOpeningInstallationTest {
    @Test
    fun finderAndTerminalAreAlwaysInstalled() {
        val calls = mutableListOf<List<String>>()
        val opening =
            MacOSEditorOpening { arguments ->
                calls += arguments
                1 to ""
            }

        assertTrue(opening.isInstalled(editor = requireNotNull(Editor.findById("finder"))))
        assertTrue(opening.isInstalled(editor = requireNotNull(Editor.findById("terminal"))))
        assertTrue(calls.isEmpty())
    }

    @Test
    fun returnsTrueWhenEditorCommandExists() {
        val calls = mutableListOf<List<String>>()
        val opening =
            MacOSEditorOpening { arguments ->
                calls += arguments
                if (arguments == listOf("which", "code")) 0 to "/usr/local/bin/code" else 1 to ""
            }

        assertTrue(opening.isInstalled(editor = requireNotNull(Editor.findById("vscode"))))
        assertEquals(listOf(listOf("which", "code")), calls)
    }

    @Test
    fun fallsBackToLaunchServicesLookupWhenCommandMissing() {
        val calls = mutableListOf<List<String>>()
        val editor =
            Editor(
                id = "custom",
                name = "Custom",
                command = "custom-cli",
                icon = "hammer",
                appName = "Custom App",
            )
        val opening =
            MacOSEditorOpening { arguments ->
                calls += arguments
                when (arguments) {
                    listOf("which", "custom-cli") -> 1 to ""
                    listOf("open", "-Ra", "Custom App") -> 0 to ""
                    else -> 1 to ""
                }
            }

        assertTrue(opening.isInstalled(editor = editor))
        assertEquals(
            listOf(
                listOf("which", "custom-cli"),
                listOf("open", "-Ra", "Custom App"),
            ),
            calls,
        )
    }

    @Test
    fun returnsFalseWhenEditorCannotBeResolved() {
        val calls = mutableListOf<List<String>>()
        val editor =
            Editor(
                id = "ghost",
                name = "Ghost",
                command = "ghost-cli",
                icon = "bolt",
                appName = "Ghost App",
            )
        val opening =
            MacOSEditorOpening { arguments ->
                calls += arguments
                1 to ""
            }

        assertFalse(opening.isInstalled(editor = editor))
        assertEquals(
            listOf(
                listOf("which", "ghost-cli"),
                listOf("open", "-Ra", "Ghost App"),
            ),
            calls,
        )
    }
}
