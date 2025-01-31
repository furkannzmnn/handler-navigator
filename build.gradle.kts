plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("java")
}

group = "com.modulith"
version = "2.1-prod-latest"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2024.3.2")
        bundledPlugin("com.intellij.java")
    }
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("junit:junit:4.13.2")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    patchPluginXml {
        sinceBuild.set("233.*")
        untilBuild.set("245.*")
    }


    publishPlugin {
        token.set("perm-b3ptZW5mOTc=.OTItMTE2NzU=.BkhTuPSXchadTP2RHDvewDO1L6BPTO")
    }
}
