package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.chassis.exceptions.BadRequestException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.ExpenseManagementClientTest.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "stubrunner.cloud.loadbalancer.enabled=false"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://konradboniecki.com.pl:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs"},
        stubsMode = REMOTE
)
public class ExpenseManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.8.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;
    @Autowired
    private ExpenseManagementClient expenseManagementClient;

    @BeforeEach
    void setUp() {
        expenseManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenTwoExpensesInBudget_whenFetchExpenses_thenReturnList() {
        // Given:
        String budgetId = "613c436d-ca18-4f31-9088-90efb19efd54";
        // When:
        List<Expense> expenseList = expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
        // Then:
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.size()).isEqualTo(2);
        Expense firstElement = expenseList.get(0);
        Expense secondElement = expenseList.get(1);
        Assertions.assertAll(
                () -> assertThat(firstElement.getId()).isEqualTo("52dc50fd-1dd1-4e62-bbab-2485f22f28ce"),
                () -> assertThat(firstElement.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(firstElement.getAmount()).isEqualTo(3L),
                () -> assertThat(firstElement.getComment()).isEqualTo("test_comments_1"),
                () -> assertThat(firstElement.getCreated()).isEqualTo(Instant.parse("2019-06-16T10:22:54.246625Z")),
                () -> assertThat(secondElement.getId()).isEqualTo("896ffae8-0a10-46e1-933a-927a417cf447"),
                () -> assertThat(secondElement.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(secondElement.getAmount()).isEqualTo(4L),
                () -> assertThat(secondElement.getComment()).isEqualTo("test_comments_2"),
                () -> assertThat(secondElement.getCreated()).isEqualTo(Instant.parse("2019-06-16T10:28:23.053553Z"))
        );
    }

    @Test
    void givenNoExpensesInBudget_whenFetchExpenses_thenReturnEmptyList() {
        // Given:
        String budgetId = "80adeba9-8ed6-4207-be1e-a1019439c0b5";
        // When:
        List<Expense> expenseList = expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
        // Then:
        assertThat(expenseList).isNotNull();
        assertThat(expenseList.size()).isEqualTo(0);
    }

    @Test
    void givenNoExpenseInBudget_whenRemoveExpense_thenReturnFalse() {
        // Given:
        String budgetId = "19e8147b-f6cb-46fa-b1d4-a0cb1ead4a08";
        String absentExpenseId = "570df03f-a98e-4752-bdab-3f7fa67e7945";
        // When:
        boolean isDeleted = expenseManagementClient.deleteExpenseInBudget(absentExpenseId, budgetId);
        // Then:
        assertThat(isDeleted).isFalse();
    }

    @Test
    void givenExpenseInBudget_whenRemoveExpense_thenReturnTrue() {
        // Given:
        String budgetId = "19e8147b-f6cb-46fa-b1d4-a0cb1ead4a08";
        String absentExpenseId = "445598c4-480a-452e-9493-8bc7ba709858";
        // When:
        boolean isDeleted = expenseManagementClient.deleteExpenseInBudget(absentExpenseId, budgetId);
        // Then:
        assertThat(isDeleted).isTrue();
    }

    @Test
    void givenValidExpense_whenSaveExpense_thenReturnExpense() {
        // Given:
        String budgetId = "9ab79704-6682-4647-ade6-ac03aaaad427";
        Expense expense = new Expense()
                .setBudgetId(budgetId)
                .setComment("testComment")
                .setAmount(3.0);
        // When:
        Expense savedExpense = expenseManagementClient.saveExpense(expense, budgetId);
        // Then:
        assertThat(savedExpense).isNotNull();
        Assertions.assertAll(
                () -> assertThat(UUID.fromString(savedExpense.getId())).isNotNull(),
                () -> assertThat(savedExpense.getComment()).isEqualTo("testComment"),
                () -> assertThat(savedExpense.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(savedExpense.getAmount()).isEqualTo(3.0),
                () -> assertThat(savedExpense.getCreated()).isInstanceOf(Instant.class)
        );
    }

    @Test
    void givenInvalidBudgetInPathAndBody_whenSave_thenThrow() {
        // Given:
        String budgetIdInPath = "e0860e06-9a46-4971-a447-07d46df471ae";
        String budgetIdInBody = "230bc5b4-68c2-4a67-af09-3fdec0599de1";
        Expense expense = new Expense()
                .setBudgetId(budgetIdInBody)
                .setAmount(3.0);
        // When:
        Throwable throwable = catchThrowable(() -> expenseManagementClient.saveExpense(expense, budgetIdInPath));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(BadRequestException.class);
    }
}
