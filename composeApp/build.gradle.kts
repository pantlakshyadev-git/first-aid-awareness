import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    jvm("desktop")

    // Apple Silicon only. Intel (x64) iOS simulators are no longer needed.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "FirstAidAwarenessKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies { implementation("androidx.activity:activity-compose:1.10.1") }
        named("desktopMain") {
            dependencies { implementation(compose.desktop.currentOs) }
        }
    }
}

android {
    namespace = "com.ben.firstaid"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.ben.firstaid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
            assets.srcDirs("src/androidMain/assets")
        }
    }
    buildTypes {
        debug {
            isCrunchPngs = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.ben.firstaid.generated.resources"
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "com.ben.firstaid.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "First Aid Awareness"
            packageVersion = "1.0.0"
            // Debian package names must be lowercase with no spaces.
            linux { packageName = "first-aid-awareness"
                iconFile.set(
                    project.file("src/desktopMain/resources/ic_launcher.png")
                )
            }
            windows {
                menuGroup = "First Aid Awareness"
                upgradeUuid = "990866cf-afe9-475c-b6f7-5446c605beae"
                iconFile.set(
                    project.file("src/desktopMain/resources/ic_launcher.ico")
                )
            }
            macOS { bundleID = "com.ben.firstaid"
                iconFile.set(
                    project.file("src/desktopMain/resources/ic_launcher.png")
                )
            }
        }
    }
}

tasks.register("packageIpa") {
    group = "package"
    description = "Packages the release iOS framework into a downloadable .ipa payload file."
    dependsOn("linkReleaseFrameworkIosArm64")
    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        val payloadDir = File(buildDir, "ipa/Payload/FirstAidAwareness.app")
        payloadDir.mkdirs()
        val frameworkDir = File(buildDir, "bin/iosArm64/releaseFramework/FirstAidAwarenessKit.framework")
        if (frameworkDir.exists()) {
            frameworkDir.copyRecursively(File(payloadDir, "FirstAidAwarenessKit.framework"), overwrite = true)
        }
        val plistFile = File(payloadDir, "Info.plist")
        plistFile.writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>CFBundleExecutable</key>
                <string>FirstAidAwareness</string>
                <key>CFBundleIdentifier</key>
                <string>com.ben.firstaid</string>
                <key>CFBundleName</key>
                <string>First Aid Awareness</string>
                <key>CFBundlePackageType</key>
                <string>APPL</string>
                <key>CFBundleShortVersionString</key>
                <string>1.0.0</string>
                <key>CFBundleVersion</key>
                <string>1</string>
                <key>LSRequiresIPhoneOS</key>
                <true/>
                <key>UIRequiredDeviceCapabilities</key>
                <array>
                    <string>arm64</string>
                </array>
            </dict>
            </plist>
            """.trimIndent()
        )
    }
}
