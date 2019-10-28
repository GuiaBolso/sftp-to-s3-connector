import com.novoda.gradle.release.PublishExtension
import io.gitlab.arturbosch.detekt.detekt
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    
    dependencies {
        classpath("com.novoda:bintray-release:0.9.1")
    }
}


plugins {
    kotlin("jvm") version "1.3.50"
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.17"
    id("io.gitlab.arturbosch.detekt").version("1.1.1")
}

apply(plugin = "com.novoda.bintray-release")

group = "br.com.guiabolso"
version = "0.1.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    
    // SFTP
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.apache.commons:commons-vfs2:2.4.1")
    
    testImplementation("com.github.stefanbirkner:fake-sftp-server-lambda:1.0.0")
    
    // S3
    implementation("com.amazonaws:aws-java-sdk-s3:1.11.488")
    testImplementation("com.adobe.testing:s3mock-junit5:2.1.16")
    
    // KotlinTest
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    val javadoc = tasks["dokka"] as DokkaTask
    javadoc.outputFormat = "javadoc"
    javadoc.outputDirectory = "$buildDir/javadoc"
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.outputDirectory)
}

detekt {
    toolVersion = "1.1.1"
    input = files("src/main/kotlin", "src/test/kotlin")
}

publishing {
    publications {
        
        register("maven", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            
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
            }
        }
    }
}

configure<PublishExtension> {
    artifactId = "sftp-to-s3-connector"
    autoPublish = true
    desc = "SFTP to Amazon S3 connector"
    groupId = "br.com.guiabolso"
    userOrg = "gb-opensource"
    setLicences("APACHE-2.0")
    publishVersion = version.toString()
    uploadName = "SFTP-to-S3-Connector"
    website = "https://github.com/GuiaBolso/sftp-to-s3-connector"
    setPublications("maven")
}
