package app.tich.buildandrun.domain.shared.model

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString
