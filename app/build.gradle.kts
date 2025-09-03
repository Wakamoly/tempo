plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        versionCode = 31
        versionName = "3.14.8"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    namespace = "com.cappielloantonio.tempo"
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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
