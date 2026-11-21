plugins {
    java
}

group = "ru.sveshnikov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "1g"
}

tasks.register<JavaExec>("bench") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "quicksort.Application"
    maxHeapSize = "3g"
}
