import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
}

group = "app.sonderful"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("no.tornado", "tornadofx", "1.7.15")
    compile("net.java.dev.jna", "jna", "5.2.0")
    compile("net.java.dev.jna", "jna-platform", "5.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Wheelie Backflipper"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "app.sonderful.wheeliebackflipper.Launcher"
    }
    from(configurations.runtime.map { if (it.isDirectory) it else zipTree(it)})
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}