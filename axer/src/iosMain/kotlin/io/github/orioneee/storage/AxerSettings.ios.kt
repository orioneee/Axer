package io.github.orioneee.storage

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings

actual fun createSettings(name: String): ObservableSettings {
    val factory = NSUserDefaultsSettings.Factory()
    return factory.create(name)
}