import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlinter)
}

detekt {
    autoCorrect = true
    source.setFrom(
        "app/src/main",
        "app/src/notquitemy",
        "app/src/play",
        "app/src/tempo",
    )
    basePath = projectDir.toString()
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt/detekt.yml")
}

dependencies {
    // Detekt
    detektPlugins(libs.detekt.formatting)
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36

        versionCode = 32
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    flavorDimensions += "default"

    productFlavors {
        create("tempo") {
            dimension = "default"
            applicationId = "com.cappielloantonio.tempo"
        }

        create("notquitemy") {
            dimension = "default"
            applicationId = "com.cappielloantonio.notquitemy.tempo"
        }

        create("play") {
            dimension = "default"
            applicationId = "com.cappielloantonio.play.tempo"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // TODO (BA, 9/4/25): Necessary?
        //isCoreLibraryDesugaringEnabled = true // Required for kotlinx-datetime support on API 24 and 25
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    namespace = "com.cappielloantonio.tempo"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "../libs", "include" to listOf("*.aar"))))

    // AndroidX
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.gson)
    implementation(libs.coil)
    implementation(libs.coil.svg)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.work.runtime.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.startup.runtime)

    // Material
    implementation(libs.material)

    // Glide
    implementation(libs.glide)
    implementation(libs.glide.annotations)
    annotationProcessor(libs.glide.compiler)

    // Media3
    implementation(libs.media3.session)
    implementation(libs.media3.common)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer.hls)
    "tempoImplementation"(libs.media3.cast)
    "playImplementation"(libs.media3.cast)

    // Room
    annotationProcessor(libs.androidx.room.compiler)

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.converter.gson)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)
    testImplementation(project.dependencies.platform(libs.koin.bom))
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    androidTestImplementation(project.dependencies.platform(libs.koin.bom))

    // Additional testing libraries
    testImplementation(libs.core.testing)
    implementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestUtil(libs.android.test.orchestrator)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
