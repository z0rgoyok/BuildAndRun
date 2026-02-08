package app.tich.buildandrun.macos

import app.tich.buildandrun.domain.ports.SystemOpening

class MacOSSystemOpening : SystemOpening {
    override fun openURL(url: String) {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isBlank()) {
            return
        }
        runShellCommand(arguments = listOf("open", normalizedUrl))
    }

    override fun revealInFinder(path: String) {
        val normalizedPath = path.trim()
        if (normalizedPath.isBlank()) {
            return
        }
        runShellCommand(arguments = listOf("open", "-R", normalizedPath))
    }

    override fun openTerminal(atPath: String) {
        val normalizedPath = atPath.trim()
        if (normalizedPath.isBlank()) {
            return
        }
        runShellCommand(arguments = listOf("open", "-a", "Terminal", normalizedPath))
    }
}
