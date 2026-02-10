// ======================================================
// 🔧 BUILD SCRIPT – classpath pro Flyway Gradle plugin
// ======================================================
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.flywaydb:flyway-database-postgresql:11.7.2")
		classpath("org.postgresql:postgresql:42.7.3")
	}
}

plugins {
	java
	id("org.springframework.boot") version "3.5.10"
	id("io.spring.dependency-management") version "1.1.7"

	// ✅ Flyway Gradle plugin
	id("org.flywaydb.flyway") version "11.7.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Offer management system"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	// ================= WEB =================
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

	// ================= DATA & SECURITY =================
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// ================= VALIDATION =================
	// ✅ SPRING WAY (ne přímo Hibernate)
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// ================= MAIL (HTML EMAILY) =================
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework:spring-context-support")

	// ================= PDF =================
	implementation("com.itextpdf:itextpdf:5.5.13.3")
	implementation("com.github.librepdf:openpdf:1.3.30")

	// ================= DB & MIGRATIONS =================
	implementation("org.postgresql:postgresql:42.7.3")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	// ================= TESTS =================
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// ======================================================
// 🛫 FLYWAY GRADLE CONFIG
// ======================================================
flyway {
	url = "jdbc:postgresql://localhost:5432/offers_db"
	user = "postgres"
	password = "postgres"
	schemas = arrayOf("public")
	locations = arrayOf("filesystem:src/main/resources/db/migration")

	// ⚠️ DEV ONLY
	cleanDisabled = false
}