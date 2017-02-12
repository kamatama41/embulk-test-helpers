[![CircleCI](https://circleci.com/gh/kamatama41/embulk-test-helpers.svg?style=svg)](https://circleci.com/gh/kamatama41/embulk-test-helpers)

# embulk-test-helpers
Helper classes for Unit test of [Embulk](http://www.embulk.org) plugins.

## Installation
Add the snippets to your build.gradle
```gradle
repositories {
    ....
    maven { url 'http://kamatama41.github.com/embulk-test-helpers/repository' }
    // If you want to use a snapshot version
    maven { url 'https://raw.github.com/kamatama41/embulk-test-helpers/snapshot/repository' }
}

dependencies {
    ....
    testCompile 'com.kamatama41:embulk-test-helpers:0.2.0'
}
```

## Usage
Please refer to sample test cases
- [Java](https://github.com/kamatama41/embulk-test-helpers/tree/master/src/test/java/com/kamatama41/embulk/test)
- [Kotlin](https://github.com/kamatama41/embulk-test-helpers/tree/master/src/test/kotlin/com/kamatama41/embulk/test)
