package app.tich.buildandrun.presentation.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

internal fun defaultAppComponentContext(): ComponentContext =
    DefaultComponentContext(
        lifecycle = LifecycleRegistry(),
    )
