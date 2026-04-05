plugins {
    java
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

version = "1.0.0"
group = "com.nyssan.client"

base {
    archivesName.set("nyssan-client")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:yarn:1.21.11+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mod_name", "NYSSAN")

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "mod_name" to "NYSSAN",
            "description" to "A premium PvP Client for Minecraft 1.21.11 - Dedicated to Technoblade"
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
