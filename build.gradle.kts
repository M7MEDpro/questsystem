plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "dev.m7med"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("studio.mevera:imperat-core:3.0.0")
    implementation("studio.mevera:imperat-bukkit:3.0.0")
    implementation("org.mongodb:mongodb-driver-sync:5.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        relocate("studio.mevera.imperat", "dev.m7med.questsystem.libs.imperat")
        relocate("com.mongodb", "dev.m7med.questsystem.libs.mongodb")
        relocate("org.bson", "dev.m7med.questsystem.libs.bson")
        archiveClassifier = ""
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21")
    }
}