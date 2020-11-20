plugins {
    java
}

group = "mod.wurmunlimited.npcs.toolpurchaser"
version = "0.1"
val shortName = "toolpurchaser"
val wurmServerFolder = "E:/Steam/steamapps/common/Wurm Unlimited/WurmServerLauncher/"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":WurmTestingHelper"))
    implementation(project(":BMLBuilder"))
    implementation(fileTree(wurmServerFolder) { include("server.jar") })
    implementation(fileTree(wurmServerFolder) { include("modlauncher.jar", "javassist.jar") })
//    implementation(fileTree(wurmServerFolder + "lib/") { include("WurmUnlimitedCommon-1.9.2.7.jar", "guava-18.0.jar",
//            "sqlite-jdbc-3.8.11.2.jar", "flyway-core-4.0.3.jar", "ServerLauncher-0.43.jar", "javassist-3.23.1.jar", "annotations-16.0.2.jar") })
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    jar {
        doLast {
            copy {
                from(jar)
                into(wurmServerFolder + "mods/" + shortName)
            }

            copy {
                from("src/main/resources/$shortName.properties")
                into(wurmServerFolder + "mods/")
            }

            copy {
                from("src/main/resources/MaterialPrices.properties")
                into(wurmServerFolder + "mods/$shortName/")
            }

            copy {
                from("src/main/resources/EnchantmentPrices.properties")
                into(wurmServerFolder + "mods/$shortName/")
            }
        }

        from(configurations.runtimeClasspath.get().filter { it.name.startsWith("BMLBuilder") && it.name.endsWith("jar") }.map { zipTree(it) })

        includeEmptyDirs = false
        archiveFileName.set("$shortName.jar")
        exclude("**/TradeHandler.class", "**/Trade.class", "**/TradingWindow.class")

        manifest {
            attributes["Implementation-Version"] = version
        }
    }

    register<Zip>("zip") {
        into(shortName) {
            from(jar)
        }

        from("src/main/resources/$shortName.properties")
        from("src/main/resources/MaterialPrices.properties")
        from("src/main/resources/EnchantmentPrices.properties")
        archiveFileName.set("$shortName.zip")
    }
}