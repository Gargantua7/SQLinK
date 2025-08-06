
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.gargantua7.sqlink"
version = "1.0.0"

kotlin {

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()
    linuxX64()

    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()

    sourceSets {

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {

    namespace = "com.gargantua7.sqlink"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {

    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "sqlink", version.toString())

    pom {
        name = "SQLinK"
        description = "SQL dynamic splicing with Kotlin DSL without platform dependency or third-party dependency"
        inceptionYear = "2025"
        url = "https://github.com/Gargantua7/SQLinK"

        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "Gargantua7"
                url = "https://github.com/Gargantua7"
                email = "gargantua7@qq.com"
            }
        }

        scm {
            url = "https://github.com/Gargantua7/SQLinK"
            connection = "scm:git:https://github.com/Gargantua7/SQLinK.git"
            developerConnection = "scm:git:ssh://git@github.com:Gargantua7/SQLinK.git"
        }
    }
}