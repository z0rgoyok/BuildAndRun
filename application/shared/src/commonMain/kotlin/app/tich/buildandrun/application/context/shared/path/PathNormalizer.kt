package app.tich.buildandrun.application.context.shared.path

fun normalizePath(path: String): String = path.trim().trimEnd('/')
