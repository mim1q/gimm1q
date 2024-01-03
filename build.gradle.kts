plugins {
    id("fabric-loom") version Versions.LOOM
}

version = ModData.VERSION
group = ModData.GROUP

repositories {
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}:v2")
    modImplementation("net.fabricmc:fabric-loader:${Versions.FABRIC_LOADER}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC_API}")

    testImplementation(sourceSets.main.get().output)
    testImplementation(sourceSets.main.get().output)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
    jar {
        from("LICENSE") {
            rename { "${it}_${ModData.GROUP}"}
        }
    }
    processResources {
        inputs.property("version", ModData.VERSION)
        inputs.property("minecraft_version", Versions.MINECRAFT)
        inputs.property("loader_version", Versions.FABRIC_LOADER)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                "version" to ModData.VERSION,
                "minecraft_version" to Versions.MINECRAFT,
                "loader_version" to Versions.FABRIC_LOADER
            )
        }
    }
}

sourceSets {
    create("testmod") {
        runtimeClasspath += main.get().runtimeClasspath
        compileClasspath += main.get().compileClasspath
    }
}

loom {

    // Kotlin DSL
    runs {
        create("testmodClient") {
            client()
            ideConfigGenerated(project.rootProject == project)
            name("Testmod Client")
            source(sourceSets.getByName("testmod"))
        }
        create("testmodServer") {
            server()
            ideConfigGenerated(project.rootProject == project)
            name("Testmod Server")
            source(sourceSets.getByName("testmod"))
        }
    }
}
