package app.tich.buildandrun.domain.ports

interface SystemOpening {
    fun openURL(url: String)

    fun revealInFinder(path: String)

    fun openTerminal(atPath: String)
}
