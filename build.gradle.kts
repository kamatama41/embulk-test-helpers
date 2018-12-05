import com.github.kamatama41.gradle.gitrelease.GitReleaseExtension

buildscript {
    repositories {
        jcenter()
        maven { setUrl("http://kamatama41.github.com/maven-repository/repository") }
    }
    dependencies {
        classpath("com.github.kamatama41:gradle-git-release-plugin:0.2.0")
    }
}

plugins {
    idea
    kotlin("jvm") version "1.2.31"
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
    compile(kotlin("stdlib"))
    compile("org.embulk:embulk-standards:0.9.7")
    compile("org.embulk:embulk-test:0.9.7")
    testCompile("junit:junit:4.12")
}

configure<GitReleaseExtension> {
    groupId = "com.github.kamatama41"
    artifactId = "embulk-test-helpers"
    repoUri = "git@github.com:kamatama41/maven-repository.git"
    repoDir = file("${System.getProperty("user.home")}/gh-maven-repository")
}

tasks {
    named<Test>("test") {
        // Not to exceed the limit of CircleCI (4GB)
        maxHeapSize = "3g"
    }
}
