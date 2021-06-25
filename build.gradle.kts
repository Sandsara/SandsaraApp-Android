buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(Deps.Plugins.buildGradle)
        classpath(Deps.Plugins.buildKotlin)
        classpath(Deps.Plugins.kotlinSerialization)

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
    }
}

tasks.register("clean").configure {
    delete("build")
}