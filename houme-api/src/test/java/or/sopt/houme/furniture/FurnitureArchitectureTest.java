package or.sopt.houme.furniture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * furniture 슬라이스(#582)의 레이어 의존 규칙을 컴파일된 바이트코드로 강제한다.
 * credit 파일럿({@code CreditArchitectureTest})과 동일한 기준을 적용한다.
 *
 * <p>주의: {@code or.sopt.houme.domain.furniture} 하위(레거시 서비스/엔티티)는 아직 헥사고날 전환 전이므로
 * 규칙 대상은 신설 슬라이스인 {@code or.sopt.houme.furniture} 로 한정한다.
 */
@DisplayName("furniture 헥사고날 레이어 아키텍처 규칙")
class FurnitureArchitectureTest {

    private static JavaClasses furnitureClasses;

    @BeforeAll
    static void importClasses() {
        furnitureClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("or.sopt.houme.furniture");
    }

    @Test
    @DisplayName("domain 은 JPA(jakarta.persistence)에 의존하지 않는다")
    void domainHasNoJpaDependency() {
        noClasses().that().resideInAPackage("..furniture.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(furnitureClasses);
    }

    @Test
    @DisplayName("domain 은 Spring 프레임워크에 의존하지 않는다")
    void domainHasNoSpringDependency() {
        noClasses().that().resideInAPackage("..furniture.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .check(furnitureClasses);
    }

    @Test
    @DisplayName("domain 은 infra 를 몰라야 한다 (의존 방향: 안쪽으로만)")
    void domainDoesNotDependOnInfra() {
        noClasses().that().resideInAPackage("..furniture.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..furniture.infra..")
                .check(furnitureClasses);
    }
}
