package app.tich.buildandrun.domain.shared.model

import java.util.UUID

actual fun generateUuid(): String = UUID.randomUUID().toString()
