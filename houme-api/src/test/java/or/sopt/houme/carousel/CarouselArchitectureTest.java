package or.sopt.houme.carousel;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * carousel 슬라이스(#582)의 레이어 의존 규칙을 컴파일된 바이트코드로 강제한다.
 * credit/tag/taste/tastetag 와 동일 기준.
 */
@DisplayName("carousel 헥사고날 레이어 아키텍처 규칙")
class CarouselArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("or.sopt.houme.carousel");
    }

    @Test
    @DisplayName("domain 은 JPA(jakarta.persistence)에 의존하지 않는다")
    void domainHasNoJpaDependency() {
        noClasses().that().resideInAPackage("..carousel.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(classes);
    }

    @Test
    @DisplayName("domain 은 Spring 프레임워크에 의존하지 않는다")
    void domainHasNoSpringDependency() {
        noClasses().that().resideInAPackage("..carousel.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(classes);
    }

    @Test
    @DisplayName("domain 은 infra 를 몰라야 한다 (의존 방향: 안쪽으로만)")
    void domainDoesNotDependOnInfra() {
        noClasses().that().resideInAPackage("..carousel.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..carousel.infra..")
                .check(classes);
    }
}
