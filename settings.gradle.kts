plugins {
    // Lets Gradle auto-provision a Java 25 toolchain when the host JDK is a
    // different version, so `./gradlew build` works the same on every machine.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "universaladmin-tab"
