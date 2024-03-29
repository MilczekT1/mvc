package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetManagementClientTest.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "stubrunner.cloud.loadbalancer.enabled=false",
                "stubrunner.cloud.delegate.enabled=true"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://konradboniecki.com.pl:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs"},
        stubsMode = REMOTE
)
class BudgetManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.9.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private BudgetManagementClient budgetManagementClient;

    @BeforeEach
    void setUp() {
        budgetManagementClient.setGatewayUrl("http://localhost:" + stubRunnerPort);
    }


    @Test
    void givenFailure_whenSaveBudget_thenRethrow500() {
        // Given:
        Budget budgetToSave = new Budget()
                .setMaxJars(8L)
                .setFamilyId("c2d8fd47-75ce-4797-9512-55d73dbeb015");
        // When:
        Throwable throwable = catchThrowable(
                () -> budgetManagementClient.saveBudget(budgetToSave));
        // Then:
        assertThat(throwable).isNotNull()
                .isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    void givenSuccess_whenSaveBudget_thenReturnBudget() {
        // Given:
        String familyId = "6537138e-1056-45be-bf24-efadbedb428b";
        Budget budgetToSave = new Budget()
                .setMaxJars(6L)
                .setFamilyId(familyId);
        // When:
        Budget budget = budgetManagementClient.saveBudget(budgetToSave);
        // Then:
        assertThat(budget).isNotNull();
        assertThat(UUID.fromString(budget.getId())).isNotNull();
        assertThat(budget.getFamilyId()).isEqualTo(familyId);
        assertThat(budget.getMaxJars()).isEqualTo(6L);
    }

    @Test
    void givenPresentFamily_whenFind_thenReturnFamily() {
        // Given:
        String presentFamilyId = "1d6030f4-9051-4d1b-8b77-4fecd1ab9a52";
        // When:
        Optional<Budget> budgetOptional = budgetManagementClient.findBudgetByFamilyId(presentFamilyId);
        // Then:
        assertThat(budgetOptional).isPresent();
        Budget budget = budgetOptional.get();
        assertThat(UUID.fromString(budget.getId())).isNotNull();
        assertThat(budget.getFamilyId()).isEqualTo(presentFamilyId);
        assertThat(budget.getMaxJars()).isEqualTo(6L);
    }

    @Test
    void givenAbsentFamily_whenFind_thenReturnEmpty() {
        // Given:
        String absentFamilyId = "b0492093-d920-492c-b5de-6e4046962410";
        // When:
        Optional<Budget> budgetOptional = budgetManagementClient.findBudgetByFamilyId(absentFamilyId);
        // Then:
        assertThat(budgetOptional).isNotPresent();
    }
}
