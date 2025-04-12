plugins {
    kotlin("jvm") version "1.9.23"
    id("java")
    id("application")
    //id("org.gradlex.extra-java-module-info") version "1.11"
    id("maven-publish")
}

group = "top.mcfpp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}


tasks.jar{
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

//extraJavaModuleInfo{
//    module("brigadier-1.3.10.jar","brigadier","1.3.10")
//    module("datafixerupper-8.0.16.jar","datafixerupper","8.0.16"){
//        requires("com.google.gson")
//        requires("com.google.common")
//        requires("it.unimi.dsi.fastutil")
//        requires("org.slf4j")
//        exportAllPackages()
//    }
//    module("annotations-13.0.jar","annotations","13.0")
//    module("listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar", "com.google.guava.listenablefuture", "9999.0")
//    module("jsr305-3.0.2.jar", "jsr305", "3.0.2")
//}


dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.google.guava:guava:33.4.5-jre")
    implementation("com.mojang:brigadier:1.3.10")
    implementation("com.mojang:datafixerupper:8.0.16")
}

tasks.test {
    useJUnitPlatform()
    modularity.inferModulePath = true
}
kotlin {
    jvmToolchain(21)
}

tasks.compileKotlin{
    kotlinOptions{
        jvmTarget = "21"
    }
}

java {
    modularity.inferModulePath = true
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "top.mcfpp"
            artifactId = "nbt"
            version = "1.0"
        }
    }
    repositories {
        mavenLocal()
    }
}