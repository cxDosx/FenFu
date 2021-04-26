import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}
group = "cxdosx.moe"
version = "2.0"

repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    val miraiVersion = "1.3.1"

    testImplementation(kotlin("test-junit"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")

    // Mirai
    implementation("net.mamoe:mirai-core:$miraiVersion")
    implementation("net.mamoe:mirai-core-qqandroid:$miraiVersion")
    implementation("io.ktor:ktor-client-serialization:1.4.0")

    // JDBC
    implementation("mysql", "mysql-connector-java", "8.0.11")

    // OKHTTP
    implementation("com.squareup.okhttp3", "okhttp", "4.8.1")

    // GSON
    implementation("com.google.code.gson", "gson", "2.8.6")

    //Jsoup
    implementation("org.jsoup", "jsoup", "1.12.1")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "moe.cxdosx.fenfu.LoaderKt"
    }
}