plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.codixly.docbot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codixly.docbot"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Add keep rules for R8
            proguardFile("r8-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }


    packaging {
        resources {
            pickFirsts += setOf("META-INF/INDEX.LIST")
            excludes += setOf("META-INF/LICENSE.txt", "META-INF/NOTICE.txt")
        }
    }

    // Add this to properly handle AAR files
    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {

//    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-ktx:1.12.0")

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    implementation(files("libs/ezdxlib.aar"))
    implementation(files("libs/ezdxlib+.aar"))
    implementation("org.pytorch:pytorch_android:2.1.0")
    implementation("org.pytorch:pytorch_android_torchvision:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.20")
    implementation("com.h6ah4i.android:openslmediaplayer:0.7.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.6.0")
    implementation("org.apache.commons:commons-compress:1.16.1")
    implementation("com.itextpdf:itextpdf:5.5.4")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.errorprone:error_prone_annotations:2.3.4")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.auto.value:auto-value-annotations:1.9")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.elinkthings:AILinkSDKRepositoryAndroid:1.10.9")
//    implementation ("com.github.elinkthings:AILinkSDKRepositoryAndroid:1.10.9")
    implementation ("com.github.elinkthings:AILinkSDKParsingLibraryAndroid:1.9.8")
    implementation ("com.google.android.material:material:1.11.0")

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

}