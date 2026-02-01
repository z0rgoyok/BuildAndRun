package app.tich.buildandrun.domain.entities

import java.util.UUID

actual fun generateUuid(): String = UUID.randomUUID().toString()
