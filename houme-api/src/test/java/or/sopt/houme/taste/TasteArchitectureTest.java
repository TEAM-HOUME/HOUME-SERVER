package or.sopt.houme.taste;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * taste 슬라이스(#582)의 레이어 의존 규칙을 컴파일된 바이트코드로 강제한다.
 * credit/tag 와 동일 기준.
 */
@DisplayName("taste 헥사고날 레이어 아키텍처 규칙")
class TasteArchitectureTest {

    private static JavaClasses tasteClasses;

    @BeforeAll
    static void importClasses() {
        tasteClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("or.sopt.houme.taste");
    }

    @Test
    @DisplayName("domain 은 JPA(jakarta.persistence)에 의존하지 않는다")
    void domainHasNoJpaDependency() {
        noClasses().that().resideInAPackage("..taste.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(tasteClasses);
    }

    @Test
    @DisplayName("domain 은 Spring 프레임워크에 의존하지 않는다")
    void domainHasNoSpringDependency() {
        noClasses().that().resideInAPackage("..taste.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(tasteClasses);
    }

    @Test
    @DisplayName("domain 은 infra 를 몰라야 한다 (의존 방향: 안쪽으로만)")
    void domainDoesNotDependOnInfra() {
        noClasses().that().resideInAPackage("..taste.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..taste.infra..")
                .check(tasteClasses);
    }
}
