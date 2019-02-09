package com.soywiz.korlibs

import com.soywiz.korlibs.modules.*
import com.soywiz.korlibs.targets.*
import org.gradle.api.*
import java.io.*

class KorlibsPlugin : Plugin<Project> {
    override fun apply(project: Project) = project {
        val korlibs = KorlibsExtension(this)
        extensions.add("korlibs", korlibs)

        plugins.apply("kotlin-multiplatform")
        plugins.apply("com.moowork.node")

        configureKorlibsRepos()

        // Platforms
        configureTargetCommon()
        configureTargetAndroid()
        configureTargetNative()
        configureTargetJavaScript()
        configureTargetJVM()

        // Publishing
        configurePublishing()
    }
}

class KorlibsExtension(val project: Project) {
    var hasAndroid = (System.getProperty("sdk.dir") != null) || (System.getenv("ANDROID_HOME") != null)

    init {
        if (!hasAndroid) {
            val trySdkDir = File(System.getProperty("user.home") + "/Library/Android/sdk")
            if (trySdkDir.exists()) {
                File(project.rootDir, "local.properties").writeText("sdk.dir=${trySdkDir.absolutePath}")
                hasAndroid = true
            }
        }
    }

    fun dependencyProject(name: String) = project {
        dependencies {
            add("commonMainApi", project(name))
        }
    }

    fun dependencyMulti(name: String) = project {
    }

    @JvmOverloads
    fun exposeVersion(name: String = project.name) {
        project.projectDir["src/commonMain/kotlin/com/soywiz/$name/internal/${name.capitalize()}Version.kt"].text = """
            package com.soywiz.$name.internal
            internal const val ${name.toUpperCase()}_VERSION = "${project.version}"
        """.trimIndent()
    }
}

val Project.korlibs get() = extensions.getByType(KorlibsExtension::class.java)
fun Project.korlibs(callback: KorlibsExtension.() -> Unit) = korlibs.apply(callback)
val Project.hasAndroid get() = korlibs.hasAndroid
