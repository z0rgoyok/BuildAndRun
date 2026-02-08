package app.tich.buildandrun.application.ports

interface SystemOpening {
    fun openURL(url: String)

    fun revealInFinder(path: String)

    fun openTerminal(atPath: String)
}
