plugins {
    // Plugins are declared in the module build file; this file defines shared versions.
    id("com.android.application") version "8.11.1" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("org.jetbrains.compose") version "1.9.0" apply false
}
