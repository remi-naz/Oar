import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.gms.google.services)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.firebase.crashlytics)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "dev.ridill.oar"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.ridill.oar"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "GOOGLE_APIS_BASE_URL", "\"https://www.googleapis.com/\"")

        androidResources {
            localeFilters += listOf("en")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("internal") {
            dimension = "env"
            applicationIdSuffix = ".internal"
            versionCode = 1
            versionName = "0.1.0"
        }

        create("production") {
            dimension = "env"
            versionCode = 11
            versionName = "1.0.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    onVariants { variant ->
        val artifactName = "${rootProject.name}-${variant.name}-code${variant.outputs.first().versionCode.get()}-v${variant.outputs.first().versionName.get()}"

        variant.outputs.forEach { output ->
            output.outputFileName.set("${artifactName}.apk")
        }
    }
}

composeCompiler {}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-XXLanguage:+PropertyParamAnnotationDefaultTargetMode",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
            "-opt-in=androidx.compose.ui.text.ExperimentalTextApi",
            "-opt-in=androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.org.jetbrains.kotlin.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSize)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    implementation(libs.androidx.compose.material)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Dagger Hilt
    implementation(libs.com.google.dagger.hilt.android)
    ksp(libs.com.google.dagger.hilt.android.compiler)
    ksp(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room Persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Work Manager
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    ksp(libs.androidx.hilt.compiler)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // AndroidX Biometric
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.security.crypto)

    // Firebase
    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.analytics)
    implementation(libs.com.google.firebase.crashlytics)
    implementation(libs.com.google.firebase.auth)
    implementation(libs.com.google.firebase.config)

    // Google Play Services
    implementation(libs.com.google.android.gms.play.services.auth)

    // AndroidX Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playServicesAuth)
    implementation(libs.com.google.identity.googleId)

    // Retrofit
    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.okhhtp3.logging.interceptor)

    implementation(libs.io.coil.kt.compose)
    implementation(libs.com.airbnb.android.lottie.compose)
    implementation(libs.com.github.zhuinden.flow.combinetuple.kt)
    implementation(libs.com.jakewharton.timber)
    implementation(libs.com.notkamui.keval)
    implementation(libs.org.mindrot.jbcrypt)

    // Test
    testImplementation(libs.com.google.truth)
    androidTestImplementation(libs.com.google.truth)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}