import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("native.cocoapods")
}

// Version from root version.properties (CI can override -PversionCode / -PversionName)
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = Properties()
if (versionPropertiesFile.exists()) {
    versionProperties.load(versionPropertiesFile.inputStream())
}
val ciVersionCode = project.findProperty("versionCode")?.toString()?.toIntOrNull()
val ciVersionName = project.findProperty("versionName")?.toString()
val appVersionCode = ciVersionCode ?: versionProperties.getProperty("versionCode", "1").toInt()
val appVersionName = ciVersionName ?: versionProperties.getProperty("versionName", "1.0")


kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    cocoapods {
        summary = "Compose application framework"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "12.0"
        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        // Note: Google-Mobile-Ads-SDK is included directly in iosApp/Podfile
        // Removed from here to avoid cinterop issues with Xcode 26
        // The Swift bridge (AdMobViewController.swift) handles AdMob integration
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // AdMob Android (standalone without Firebase)
            implementation("com.google.android.gms:play-services-ads:23.6.0")

            // Google Play Billing for in-app purchases
            implementation("com.android.billingclient:billing-ktx:8.0.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.markduenas.android.apigen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.markduenas.android.apigen"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        // Overridden per build type; default safe for non-store installs
        buildConfigField("Boolean", "USE_TEST_ADS", "true")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("Boolean", "USE_TEST_ADS", "true")
        }
        getByName("release") {
            isMinifyEnabled = false
            // Play Production track only
            buildConfigField("Boolean", "USE_TEST_ADS", "false")
        }
        // Play Internal / Closed testing: same as release but forces Google sample ad units.
        // Upload: ./gradlew :composeApp:bundleInternal
        create("internal") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
            versionNameSuffix = "-internal"
            buildConfigField("Boolean", "USE_TEST_ADS", "true")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}