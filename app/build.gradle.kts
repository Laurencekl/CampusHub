plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.14"
}

android {
    namespace = "com.laurencekl.campushub"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.laurencekl.campushub"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")
    implementation("com.google.android.material:material:1.14.0")
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

val classesDasRegras = fileTree(
    layout.buildDirectory.dir(
        "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
    )
) {
    include("com/laurencekl/campushub/Evento.class")
    include("com/laurencekl/campushub/EventoRegras.class")
    include("com/laurencekl/campushub/UsuarioRegras.class")
}

tasks.register<JacocoReport>("relatorioCobertura") {
    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(classesDasRegras)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
        }
    )

    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("verificarCobertura") {
    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(classesDasRegras)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
        }
    )

    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
