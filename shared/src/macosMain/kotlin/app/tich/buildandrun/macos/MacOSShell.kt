package app.tich.buildandrun.macos

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
internal fun runShellCommand(
    arguments: List<String>,
    workingDirectory: String? = null,
): Pair<Int, String> {
    val commandBody = arguments.joinToString(separator = " ") { quoteForShell(it) } + " 2>&1"
    val command =
        if (workingDirectory == null) {
            commandBody
        } else {
            "cd ${quoteForShell(workingDirectory)} && $commandBody"
        }
    val stream = popen(command, "r") ?: return -1 to "failed_to_start_process"
    val output =
        buildString {
            memScoped {
                val bufferSize = 4096
                val buffer = allocArray<ByteVar>(bufferSize)
                while (true) {
                    val line = fgets(buffer, bufferSize, stream) ?: break
                    append(line.toKString())
                }
            }
        }.trim()
    return pclose(stream) to output
}

internal fun quoteForShell(argument: String): String = "'" + argument.replace("'", "'\"'\"'") + "'"
