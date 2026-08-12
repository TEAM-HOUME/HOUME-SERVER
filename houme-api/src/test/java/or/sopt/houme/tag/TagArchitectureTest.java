package or.sopt.houme.tag;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * tag 슬라이스(#582)의 레이어 의존 규칙을 컴파일된 바이트코드로 강제한다.
 * credit 파일럿({@code CreditArchitectureTest})과 동일한 기준을 적용한다.
 *
 * <ul>
 *   <li>domain(순수 모델 + port.out): JPA/Spring/infra 를 몰라야 한다</li>
 * </ul>
 */
@DisplayName("tag 헥사고날 레이어 아키텍처 규칙")
class TagArchitectureTest {

    private static JavaClasses tagClasses;

    @BeforeAll
    static void importClasses() {
        tagClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("or.sopt.houme.tag");
    }

    @Test
    @DisplayName("domain 은 JPA(jakarta.persistence)에 의존하지 않는다")
    void domainHasNoJpaDependency() {
        noClasses().that().resideInAPackage("..tag.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(tagClasses);
    }

    @Test
    @DisplayName("domain 은 Spring 프레임워크에 의존하지 않는다")
    void domainHasNoSpringDependency() {
        noClasses().that().resideInAPackage("..tag.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(tagClasses);
    }

    @Test
    @DisplayName("domain 은 infra 를 몰라야 한다 (의존 방향: 안쪽으로만)")
    void domainDoesNotDependOnInfra() {
        noClasses().that().resideInAPackage("..tag.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..tag.infra..")
                .check(tagClasses);
    }
}
