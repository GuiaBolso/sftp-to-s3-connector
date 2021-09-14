import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

plugins {
    kotlin("jvm") version "1.4.31"
    `maven-publish`
    signing
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
}

group = "br.com.guiabolso"
version = getenv("RELEASE_VERSION") ?: "local"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    // SFTP
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.apache.commons:commons-vfs2:2.4.1")

    testImplementation("com.github.stefanbirkner:fake-sftp-server-lambda:1.0.0")
    testImplementation("org.apache.sshd:sshd-sftp:2.4.0")

    // S3
    api("com.amazonaws:aws-java-sdk-s3:1.12.67")
    testImplementation("com.adobe.testing:s3mock-junit5:2.1.16")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadoc = tasks.named("javadoc")
val javadocsJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles java doc to jar"
    archiveClassifier.set("javadoc")
    from(javadoc)
}

publishing {

    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getenv("OSSRH_USERNAME")
                password = getenv("OSSRH_PASSWORD")
            }
        }
    }
    
    publications {

        register("maven", MavenPublication::class) {
            from(components["java"])
            artifact(javadocsJar)
            artifact(sourcesJar.get())

            pom {
                name.set("SFTP-to-S3-Connector")
                description.set("SFTP-to-S3-Connector")
                url.set("https://github.com/GuiaBolso/sftp-to-s3-connector")


                scm {
                    connection.set("scm:git:https://github.com/GuiaBolso/sftp-to-s3-connector/")
                    developerConnection.set("scm:git:https://github.com/GuiaBolso/")
                    url.set("https://github.com/GuiaBolso/sftp-to-s3-connector")
                }

                licenses {
                    license {
                        name.set("The Apache 2.0 License")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("Guiabolso")
                        name.set("Guiabolso")
                    }
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useGpgCmd()
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    sign((extensions.getByName("publishing") as PublishingExtension).publications)
}
