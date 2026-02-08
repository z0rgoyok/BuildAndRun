package app.tich.buildandrun.macos

import app.tich.buildandrun.application.ports.EditorOpening
import app.tich.buildandrun.domain.entities.Editor
import app.tich.buildandrun.domain.errors.AppError

class MacOSEditorOpening(
    private val executeCommand: (List<String>) -> Pair<Int, String> = { arguments ->
        runShellCommand(arguments = arguments)
    },
) : EditorOpening {
    override suspend fun open(
        path: String,
        withEditor: Editor,
    ) {
        val targetPath = path.trim()
        if (targetPath.isBlank()) {
            throw AppError.Validation(reason = AppError.ValidationReason.WORKTREE_PATH_BLANK)
        }
        val commandArguments =
            when {
                withEditor.command == "open -a Terminal" ->
                    listOf("open", "-a", "Terminal", targetPath)
                withEditor.command == "open" ->
                    listOf("open", targetPath)
                isCommandAvailable(command = withEditor.command) ->
                    listOf(withEditor.command, targetPath)
                withEditor.appName != null ->
                    listOf("open", "-a", withEditor.appName, targetPath)
                else ->
                    listOf(withEditor.command, targetPath)
            }
        val (exitCode, output) = executeCommand(commandArguments)
        if (exitCode != 0) {
            throw AppError.Unexpected(
                reason = output.ifBlank { "failed_to_open_editor" },
            )
        }
    }

    override fun availableEditors(): List<Editor> = Editor.builtIn

    override fun allEditors(): List<Editor> = Editor.builtIn

    override fun isInstalled(editor: Editor): Boolean {
        if (editor.id == "finder" || editor.id == "terminal") {
            return true
        }
        if (isCommandAvailable(command = editor.command)) {
            return true
        }
        val appName = editor.appName ?: return false
        val (exitCode, _) = executeCommand(listOf("open", "-Ra", appName))
        return exitCode == 0
    }

    private fun isCommandAvailable(command: String): Boolean {
        val commandName = command.trim().substringBefore(' ')
        if (commandName.isBlank()) {
            return false
        }
        val (exitCode, _) = executeCommand(listOf("which", commandName))
        return exitCode == 0
    }
}
