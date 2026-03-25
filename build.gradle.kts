/**
 * Top-level build file where you can add configuration options common to all sub-projects/modules.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Ensure buildscript (Gradle classpath) also uses patched dependencies to satisfy Snyk
buildscript {
    configurations.all {
        resolutionStrategy {
            // Netty family (addresses HTTP smuggling, CRLF injection, resource limits, compression issues)
            force("io.netty:netty-common:4.1.129.Final")
            force("io.netty:netty-codec-http:4.1.129.Final")
            force("io.netty:netty-codec-http2:4.1.129.Final")
            force("io.netty:netty-handler:4.1.129.Final")
            force("io.netty:netty-buffer:4.1.129.Final")
            force("io.netty:netty-transport:4.1.129.Final")

            // Protobuf (addresses stack-based buffer overflow)
            force("com.google.protobuf:protobuf-java:3.25.5")
            force("com.google.protobuf:protobuf-java-util:3.25.5")

            // Guava for buildscript should use JRE variant
            force("com.google.guava:guava:33.4.0-jre")

            // Kotlin stdlib alignment (prevents older stdlib like 1.9.0 from appearing transitively)
            force("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-common:2.1.20")
        }
    }
}

subprojects {
    // Enforce secure versions across all modules/configurations to satisfy Snyk
    configurations.all {
        resolutionStrategy {
            // Netty family (addresses HTTP smuggling, CRLF injection, resource limits, compression issues)
            force("io.netty:netty-common:4.1.129.Final")
            force("io.netty:netty-codec-http:4.1.129.Final")
            force("io.netty:netty-codec-http2:4.1.129.Final")
            force("io.netty:netty-handler:4.1.129.Final")
            force("io.netty:netty-buffer:4.1.129.Final")
            force("io.netty:netty-transport:4.1.129.Final")

            // Protobuf (addresses stack-based buffer overflow)
            force("com.google.protobuf:protobuf-java:3.25.5")
            force("com.google.protobuf:protobuf-java-util:3.25.5")

            // Guava (addresses insecure temp file creation)
            force("com.google.guava:guava:33.4.0-android")

            // Kotlin stdlib alignment (prevents older stdlib like 1.9.0 from appearing transitively)
            force("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
            force("org.jetbrains.kotlin:kotlin-stdlib-common:2.1.20")
        }
    }
}
