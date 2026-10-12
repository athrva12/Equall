plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin("multiplatform") version "2.2.10" apply false
    id("org.jetbrains.compose") version "1.9.3" apply false
    id("com.android.kotlin.multiplatform.library") version "9.1.1" apply false
}
