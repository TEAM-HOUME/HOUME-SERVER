package or.sopt.houme.credit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * credit 헥사고날 파일럿(#581)의 레이어 의존 규칙을 컴파일된 바이트코드로 강제한다.
 * 이후 전 도메인 전환(#582)의 기준 규칙이 된다.
 *
 * <ul>
 *   <li>domain: 순수 — JPA/Spring/application/infra 를 몰라야 한다</li>
 *   <li>application: infra(어댑터 구현)를 몰라야 한다 — 포트로만 소통</li>
 * </ul>
 */
@DisplayName("credit 헥사고날 레이어 아키텍처 규칙")
class CreditArchitectureTest {

    private static JavaClasses creditClasses;

    @BeforeAll
    static void importClasses() {
        creditClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("or.sopt.houme.credit");
    }

    @Test
    @DisplayName("domain 은 JPA(jakarta.persistence)에 의존하지 않는다")
    void domainHasNoJpaDependency() {
        noClasses().that().resideInAPackage("..credit.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(creditClasses);
    }

    @Test
    @DisplayName("domain 은 Spring 프레임워크에 의존하지 않는다")
    void domainHasNoSpringDependency() {
        noClasses().that().resideInAPackage("..credit.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(creditClasses);
    }

    @Test
    @DisplayName("domain 은 application/infra 를 몰라야 한다 (의존 방향: 안쪽으로만)")
    void domainDoesNotDependOnOuterLayers() {
        noClasses().that().resideInAPackage("..credit.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..credit.application..", "..credit.infra..")
                .check(creditClasses);
    }

    @Test
    @DisplayName("application 은 infra(어댑터 구현)에 의존하지 않는다 — 포트로만 소통")
    void applicationDoesNotDependOnInfra() {
        noClasses().that().resideInAPackage("..credit.application..")
                .should().dependOnClassesThat().resideInAPackage("..credit.infra..")
                .check(creditClasses);
    }
}
