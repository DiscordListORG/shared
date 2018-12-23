import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm").version("1.3.10")
    id("com.github.johnrengelman.shadow").version("4.0.3")
    application
}

group = "org.discordlist"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("commons-cli:commons-cli:1.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")
    implementation("com.github.ForYaSee:EventSystem:v1.0.0")
    implementation("net.dv8tion:JDA:3.8.1_448")
    implementation("com.github.Carleslc:Simple-YAML:1.3")
    implementation("com.google.guava:guava:27.0-jre")
    implementation("org.apache.logging.log4j:log4j-api:2.11.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")
    implementation("redis.clients:jedis:3.0.0-m1")
    implementation("com.rabbitmq:amqp-client:5.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    "shadowJar"(ShadowJar::class) {
        baseName = project.name
        version = version
        archiveName = "$baseName.$extension"
    }
}