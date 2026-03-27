package org.meldtech.platform.srta.common;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

@AnalyzeClasses(packages = "org.meldtech.platform.srta", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule controllers_must_have_pre_authorize =
            methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .and().arePublic()
                    .should().beAnnotatedWith(PreAuthorize.class)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule ban_float_and_double_in_sensitive_packages =
            fields().that().haveRawType(float.class)
                    .or().haveRawType(Double.class)
                    .or().haveRawType(double.class)
                    .or().haveRawType(Float.class)
                    .should().beDeclaredInClassesThat().resideOutsideOfPackages("..trade..", "..accounting..")
                    .allowEmptyShould(true)
                    .as("Sensitive packages (trade, accounting) should use BigDecimal for precision.");

    @ArchTest
    static final ArchRule sensitive_fields_must_have_json_ignore =
            fields().that().haveNameMatching(".*passwordHash.*")
                    .should().beAnnotatedWith(com.fasterxml.jackson.annotation.JsonIgnore.class)
                    .allowEmptyShould(true);
}
