package app.tich.buildandrun.buildandrun

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
