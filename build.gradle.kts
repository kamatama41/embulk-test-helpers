import com.github.kamatama41.gradle.gitrelease.GitReleaseExtension

buildscript {
    val kotlinVersion = "1.1.0"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        jcenter()
        // maven { setUrl("http://kamatama41.github.com/maven-repository/repository") }
         maven { setUrl("${System.getProperty("user.home")}/gh-maven-repository/repository") }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.kamatama41:gradle-git-release-plugin:0.2.0-SNAPSHOT")
    }
}

apply {
    plugin("idea")
    plugin("kotlin")
    plugin("com.github.kamatama41.git-release")
}

repositories {
    jcenter()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

val kotlinVersion: String by extra
dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("org.embulk:embulk-standards:0.8.18")
    compile("org.embulk:embulk-test:0.8.18")
    testCompile("junit:junit:4.12")
}

configure<GitReleaseExtension> {
    groupId = "com.github.kamatama41"
    artifactId = "embulk-test-helpers"
    repoUri = "git@github.com:kamatama41/maven-repository.git"
    repoDir = file("${System.getProperty("user.home")}/gh-maven-repository")
    releaseBranch = "release-test"
}
