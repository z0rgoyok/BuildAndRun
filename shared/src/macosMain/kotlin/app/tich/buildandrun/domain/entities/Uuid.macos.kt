package app.tich.buildandrun.domain.entities

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString
