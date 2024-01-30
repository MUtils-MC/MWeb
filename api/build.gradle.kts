plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    signing
}

group = "de.miraculixx.mweb.api"
setProperty("module_name", "mweb")

val githubRepo = "MUtils-MC/MWeb"
val isSnapshot = false

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    register("release") {
        group = "publishing"
        dependsOn("publish")
    }
}

publishing {
    repositories {
        maven {
            name = "ossrh"
            credentials(PasswordCredentials::class)
            setUrl(
                if (!isSnapshot) "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
                else "https://s01.oss.sonatype.org/content/repositories/snapshots"
            )
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "de.miraculixx"
            artifactId = "mweb"
            version = "1.1.0"

            from(components["java"])

            pom {
                name.set("MWeb-API")
                description.set("Access MWeb through this API")
                url.set("https://mutils.net/mweb")

                developers {
                    developer {
                        id.set("miraculixx")
                        name.set("Miraculixx")
                        email.set("miraculixxyt@gmail.com")
                    }
                }

                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.de.html")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/${githubRepo}.git")
                    url.set("https://github.com/${githubRepo}/tree/master/api")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
