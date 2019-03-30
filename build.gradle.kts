import com.github.kamatama41.gradle.gitrelease.GitReleaseExtension

plugins {
    idea
    kotlin("jvm") version "1.3.21"
    id("com.github.kamatama41.git-release") version "0.3.0"
}

apply {
    plugin("com.github.kamatama41.git-release")
}

repositories {
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.embulk:embulk-standards:0.9.15")
    implementation("org.embulk:embulk-test:0.9.15")
    implementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

configure<GitReleaseExtension> {
    groupId = "com.github.kamatama41"
    artifactId = "embulk-test-helpers"
    repoUri = "git@github.com:kamatama41/maven-repository.git"
    repoDir = file("${System.getProperty("user.home")}/gh-maven-repository")
}

tasks {
    named<Test>("test") {
        useJUnitPlatform()
        // Not to exceed the limit of CircleCI (4GB)
        maxHeapSize = "3g"
    }
}
