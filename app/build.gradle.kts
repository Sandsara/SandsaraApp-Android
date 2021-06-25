
plugins {
    id(Deps.Plugins.application)
    id(Deps.Plugins.kotlinAndroid)
    id(Deps.Plugins.kotlinKapt)
    id(Deps.Plugins.kotlinSerialize)
    id(Deps.Plugins.kotlinParcelize)

    id("com.google.gms.google-services") // Google Services Gradle plugin
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.dokka")
}

android {
    compileSdkVersion(Deps.App.compileSdk)
    buildToolsVersion(Deps.App.buildTools)

    defaultConfig {
        applicationId = Deps.App.appId
        minSdkVersion(Deps.App.minSdk)
        targetSdkVersion(Deps.App.targetSdk)
        versionCode = Deps.App.versionCode
        versionName = Deps.App.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isShrinkResources = false
            isZipAlignEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Deps.AndroidX.constraint)
    implementation(Deps.AndroidX.navFragment)
    implementation(Deps.AndroidX.navUiKtx)
    implementation(Deps.AndroidX.roomRuntime)
    implementation(Deps.AndroidX.roomKtx)
    kapt(Deps.AndroidX.roomCompiler)

    implementation(Deps.Common.material)

    implementation(Deps.AndroidX.lifeExt)
    implementation(Deps.AndroidX.lifeKtx)
    implementation(Deps.AndroidX.lifeVm)
    implementation(Deps.AndroidX.paging)

    implementation(Deps.Common.colorPicker)

    implementation(Deps.Network.serializerCore)
    implementation(Deps.Network.serializeJson)
    implementation(Deps.Network.serializeJvm)

    implementation(Deps.Network.ktorOk)
    implementation(Deps.Network.ktorLogging)
    implementation(Deps.Network.okLogger)

    implementation(Deps.Koin.core)
    implementation(Deps.Koin.scope)
    implementation(Deps.Koin.viewModel)

    implementation(Deps.Common.coil)

    implementation(Deps.Kt.stdLib)
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appCompat)

    implementation(Deps.Common.timber)

    implementation(Deps.Kt.coroutineCore)
    implementation(Deps.Kt.coroutineAndroid)

    implementation(Deps.Common.able)

    implementation(Deps.BottomSheet.core)
    implementation(Deps.BottomSheet.info)
    implementation(Deps.BottomSheet.input)

    implementation(platform(Deps.Firebase.platform))
    implementation(Deps.Firebase.crashlytic)
    implementation(Deps.Firebase.analytic)

    implementation(Deps.Test.junit4)
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    outputDirectory.set(rootDir.resolve("docs"))
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
            sourceRoots.setFrom("src/main/java")
        }
    }
}