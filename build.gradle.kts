import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.Permission
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1"
}

group = "dev.lyphium"
version = "1.1.2"
description = "Simple Boss Bar plugin"

repositories {
    mavenCentral()
    maven {
        name = "papi-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly("me.clip:placeholderapi:2.12.3")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    testCompileOnly("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    compileJava {
        options.release = 25
    }
}


paperPluginYaml {
    main = "dev.lyphium.bossbar.SimpleBossBar"
    load = BukkitPluginYaml.PluginLoadOrder.POSTWORLD
    apiVersion = "26.2"
    author = "Lyphion"
    website = "https://github.com/Lyphion/SimpleBossBar"
    dependencies.server.register("PlaceholderAPI") {
        load = PaperPluginYaml.Load.BEFORE
        required = false
    }
    permissions {
        register("bossbar.admin") {
            description = "Admin permission"
            default = Permission.Default.OP
        }
    }
}
