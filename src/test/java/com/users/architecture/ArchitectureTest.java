package com.users.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** Clean-architecture rules. A violation fails the build. */
@AnalyzeClasses(packages = "com.users", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	private static final String[] FRAMEWORKS = { "org.springframework..", "jakarta.persistence..", "jakarta.ws.rs..",
			"jakarta.servlet..", "org.keycloak..", "org.hibernate..", "tools.jackson..", "com.fasterxml..",
			"org.mapstruct..", "io.swagger..", "com.platform.." };

	@ArchTest
	static final ArchRule dependenciesPointInward = layeredArchitecture().consideringOnlyDependenciesInLayers()
			.layer("Domain").definedBy("com.users.domain..")
			.layer("Application").definedBy("com.users.application..")
			.layer("Infrastructure").definedBy("com.users.infrastructure..")
			.layer("Interfaces").definedBy("com.users.interfaces..")
			.whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
			.whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
			.whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces", "Infrastructure")
			.whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces");

	@ArchTest
	static final ArchRule domainIsPureJava = noClasses().that().resideInAPackage("com.users.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
			.because("the domain must not know Spring, JPA, Keycloak or any other framework");

	@ArchTest
	static final ArchRule domainDependsOnNothingElse = classes().that().resideInAPackage("com.users.domain..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("com.users.domain..", "java..");

	@ArchTest
	static final ArchRule applicationDependsOnlyOnDomain = classes().that().resideInAPackage("com.users.application..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("com.users.application..", "com.users.domain..", "java..");

	@ArchTest
	static final ArchRule keycloakStaysInItsAdapter = noClasses().that()
			.resideOutsideOfPackage("com.users.infrastructure.keycloak..")
			.should().dependOnClassesThat().resideInAnyPackage("org.keycloak..", "jakarta.ws.rs..")
			.because("swapping the identity provider must only touch the Keycloak adapter");

	@ArchTest
	static final ArchRule jpaStaysInPersistence = noClasses().that()
			.resideOutsideOfPackage("com.users.infrastructure.persistence..")
			.should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..");

	@ArchTest
	static final ArchRule controllersCallUseCasesNotPorts = noClasses().that().resideInAPackage("com.users.interfaces..")
			.should().dependOnClassesThat().resideInAPackage("com.users.domain.port..")
			.because("the interfaces layer goes through application use cases");
}
