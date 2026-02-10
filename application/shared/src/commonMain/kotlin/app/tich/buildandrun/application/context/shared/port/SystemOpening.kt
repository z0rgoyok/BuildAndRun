package app.tich.buildandrun.application.context.shared.port

interface SystemOpening {
    fun openURL(url: String)

    fun revealInFinder(path: String)

    fun openTerminal(atPath: String)
}
