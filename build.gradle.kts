import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
}

group = "net.azisaba"
version = "1.0-SNAPSHOT"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven {
        name = "papermc-repo"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        from(sourceSets.main.get().resources) {
            filter(ReplaceTokens::class, mapOf("tokens" to mapOf("version" to project.version)))

            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}
