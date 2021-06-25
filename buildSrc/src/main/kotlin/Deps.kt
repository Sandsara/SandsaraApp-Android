object Deps {

    const val AppSecret = "9088234f-05d3-4ce3-bbad-b28b77576d49"

    object Plugins {
        const val kotlinSerialization = "org.jetbrains.kotlin:kotlin-serialization:1.4.0"
        const val buildGradle = "com.android.tools.build:gradle:${Version.buildGradle}"
        const val buildKotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kt}"
        const val application = "com.android.application"
        const val library = "com.android.library"

        const val kotlinAndroid = "kotlin-android"
        const val kotlinParcelize = "kotlin-parcelize"
        const val kotlinKapt = "kotlin-kapt"
        const val kotlin = "kotlin"
        const val kotlinSerialize = "kotlinx-serialization"
    }

    object App {
        const val appId = "com.ht117.sandsara"
        const val minSdk = 21
        const val compileSdk = 29
        const val targetSdk = 29
        const val buildTools = "29.0.3"
        const val versionCode = 3
        const val versionName = "1.0"
    }

    object Kt {
        const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib:${Version.kt}"
        const val coroutineAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.coroutine}"
        const val coroutineCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.coroutine}"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:${Version.ktxCore}"
        const val appCompat = "androidx.appcompat:appcompat:${Version.appCompat}"
        const val constraint = "androidx.constraintlayout:constraintlayout:${Version.constraint}"
        const val navFragment = "androidx.navigation:navigation-fragment-ktx:${Version.navFragment}"
        const val navUiKtx = "androidx.navigation:navigation-ui-ktx:${Version.navUiKtx}"

        const val lifeExt = "androidx.lifecycle:lifecycle-extensions:${Version.lifeExt}"
        const val lifeVm = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.lifeExt}"
        const val lifeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifeRuntime}"

        const val roomRuntime = "androidx.room:room-runtime:${Version.room}"
        const val roomCompiler = "androidx.room:room-compiler:${Version.room}"
        const val roomKtx = "androidx.room:room-ktx:${Version.room}"

        const val paging = "androidx.paging:paging-runtime:3.0.0-alpha13"
    }

    object Common {
        const val timber = "com.jakewharton.timber:timber:${Version.timber}"
        const val material = "com.google.android.material:material:${Version.material}"

        const val coil = "io.coil-kt:coil:1.1.0"
        const val able = "com.juul.able:core:0.8.1"
        const val colorPicker = "codes.side:andcolorpicker:0.5.0"
    }

    object Firebase {
        const val platform = "com.google.firebase:firebase-bom:26.3.0"
        const val crashlytic = "com.google.firebase:firebase-crashlytics-ktx"
        const val analytic = "com.google.firebase:firebase-analytics-ktx"
    }

    object BottomSheet {
        const val core = "com.maxkeppeler.bottomsheets:core:${Version.botsheet}"
        const val info = "com.maxkeppeler.bottomsheets:info:${Version.botsheet}"
        const val input = "com.maxkeppeler.bottomsheets:input:${Version.botsheet}"
    }

    object Koin {
        const val core = "org.koin:koin-core:${Version.koin}"
        const val scope = "org.koin:koin-androidx-scope:${Version.koin}"
        const val viewModel = "org.koin:koin-androidx-viewmodel:${Version.koin}"
    }

    object Network {
        const val serializerCore = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1"
        const val serializeJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1"
        const val serializeJvm = "io.ktor:ktor-client-serialization-jvm:1.4.2"
        const val ktor = "io.ktor:ktor-client-android:1.4.2"
        const val ktorOk = "io.ktor:ktor-client-okhttp:1.4.2"
        const val ktorLogging = "io.ktor:ktor-client-logging-jvm:1.4.0"
        const val okLogger = "com.squareup.okhttp3:logging-interceptor:4.9.0"
    }

    object Test {
        const val junit4 = "junit:junit:${Version.junit}"
        const val extJunit = "androidx.test.ext:junit:${Version.extJunit}"
        const val espresso = "androidx.test.espresso:espresso-core:${Version.espresso}"
        const val koin = "org.koin:koin-test:${Version.koin}"
    }
}