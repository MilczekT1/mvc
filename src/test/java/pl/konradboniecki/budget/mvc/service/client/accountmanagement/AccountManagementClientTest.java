package pl.konradboniecki.budget.mvc.service.client.accountmanagement;

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
import org.springframework.web.client.HttpClientErrorException;
import pl.konradboniecki.budget.mvc.model.Account;

import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.accountmanagement.AccountManagementClientTest.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "stubrunner.cloud.loadbalancer.enabled=false"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://161.97.176.83:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs"},
        stubsMode = REMOTE
)
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class AccountManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "account-management";
    public static final String STUB_VERSION = "0.7.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private AccountManagementClient accountManagementClient;

    @BeforeEach
    void setUp() {
        accountManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void whenFoundAccountById_thenHandle200Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountById("ead1f1a2-d178-4204-9ec1-b78c5bf6402c");
        assertThat(accountResponse.isPresent()).isTrue();

        Account acc = accountResponse.get();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(acc.getId()).isNotNull(),
                () -> assertThat(acc.getFirstName()).isNotNull(),
                () -> assertThat(acc.getLastName()).isNotNull(),
                () -> assertThat(acc.getFamilyId()).isNotNull(),
                () -> assertThat(acc.getEmail()).isNotNull(),
                () -> assertThat(acc).isNotNull()
        );
    }

    @Test
    void whenNotFoundAccountById_thenHandle404Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountById("cc2871a2-e6b1-4490-8840-9d50502074b0");
        assertThat(accountResponse.isPresent()).isFalse();
    }

    @Test
    void whenFoundAccountByEmail_thenHandle200Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountByEmail("existing_email@find_by_mail.com");
        assertThat(accountResponse.isPresent()).isTrue();

        Account acc = accountResponse.get();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(acc.getId()).isNotNull(),
                () -> assertThat(acc.getFirstName()).isNotNull(),
                () -> assertThat(acc.getLastName()).isNotNull(),
                () -> assertThat(acc.getFamilyId()).isNotNull(),
                () -> assertThat(acc.getEmail()).isNotNull(),
                () -> assertThat(acc).isNotNull()
        );
    }

    @Test
    void whenNotFoundAccountByEmail_thenHandle404Response() {
        Optional<Account> accountResponse = accountManagementClient.findAccountByEmail("not_existing_email@mail.com");
        assertThat(accountResponse.isPresent()).isFalse();
    }

    @Test
    void whenAlreadyCreatedAccountDuringCreation_thenHandle409Response() {
        // Given:
        Account accountToSave = new Account()
                .setFirstName("mvcTestFirstName")
                .setLastName("mvcTestLastName")
                .setEmail("existing_email@mail.com")
                .setPassword("randomTestPasswd");
        // When:
        Throwable throwable = catchThrowableOfType(() ->
                        accountManagementClient.saveAccount(accountToSave),
                Throwable.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.Conflict.class);
    }

    @Test
    void whenCreatedAccount_thenHandle200Response() {
        // Given:
        Account accountToSave = new Account()
                .setFirstName("mvcTestFirstName")
                .setLastName("mvcTestLastName")
                .setEmail("not_existing_email@mail.com")
                .setPassword("randomTestPasswd");
        // When:
        Account retrievedAccount = accountManagementClient.saveAccount(accountToSave);
        // Then:
        Assertions.assertAll(
                () -> assertThat(retrievedAccount.getId()).isNotNull(),
                () -> assertThat(retrievedAccount.getFamilyId()).isNull(),
                () -> assertThat(retrievedAccount.getFirstName()).isEqualTo(accountToSave.getFirstName()),
                () -> assertThat(retrievedAccount.getLastName()).isEqualTo(accountToSave.getLastName()),
                () -> assertThat(retrievedAccount.getEmail()).isEqualTo(accountToSave.getEmail()),
                () -> assertThat(retrievedAccount.getPassword()).isNull(),
                () -> assertThat(retrievedAccount.getEmail()).isEqualTo(accountToSave.getEmail()),
                () -> assertThat(retrievedAccount.getCreated()).isNull(),
                () -> assertThat(retrievedAccount.isEnabled()).isFalse()
        );

    }

    @Test
    void whenCreatedActivationCode_thenHandle201Response() {
        // Given:
        String accIdForActivationCodeGeneration = "246b0ae2-d943-4d1a-a418-fdadfcb80455";
        // When:
        String activationCode = accountManagementClient.createActivationCodeForAccount(accIdForActivationCodeGeneration);
        // Then:
        assertThat(activationCode).isNotNull();
        assertThat(activationCode).isNotBlank();
    }

    @Test
    void whenAccountNotFoundDuringActivationCodeCreation_thenHandle404Response() {
        // Given:
        String accIdForActivationCodeGeneration = "af138a5a-365c-4708-a0b9-0df76bd6b754";
        // When:
        Throwable throwable = catchThrowableOfType(() ->
                        accountManagementClient.createActivationCodeForAccount(accIdForActivationCodeGeneration),
                Throwable.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void givenCorrectPassword_whenCheckIfPasswordIsCorrect_thenReturnTrue() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect("b6e13d85-6c2e-4635-8f76-8b36a9184c86", "correctHashValue");
        // Then:
        assertThat(isPasswordValid).isTrue();
    }

    @Test
    void givenIncorrectPassword_whenCheckIfPasswordIsCorrect_thenReturnFalse() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect("b6e13d85-6c2e-4635-8f76-8b36a9184c86", "incorrectHashValue");
        // Then:
        assertThat(isPasswordValid).isFalse();
    }

    @Test
    void givenAbsentAccount_whenCheckIfPasswordIsCorrect_thenReturnFalse() {
        // When:
        Boolean isPasswordValid = accountManagementClient.checkIfPasswordIsCorrect("bbd30a00-8d3f-4c61-8368-a10038ed2637", "notImportantHashValue");
        // Then:
        assertThat(isPasswordValid).isFalse();
    }

    @Test
    void whenFamilyAssignedToAccount_thenHandle200Response() {
        // Given:
        String accountId = "4987a33c-66c5-47e9-86a1-d4da60cc6561";
        String familyId = "7dca5ea7-fa5c-4303-9569-c2f722a8fffa";
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(familyId, accountId);
        // Then:
        assertThat(isFamilyAssigned).isTrue();
    }

    @Test
    void givenAbsentFamily_whenAssignFamilyToAccount_thenHandle404Response() {
        // Given:
        String accountId = "4987a33c-66c5-47e9-86a1-d4da60cc6561";
        String missingFamilyId = "83e61d08-7b6d-4520-bf73-7d9822bb5eca";
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(missingFamilyId, accountId);
        // Then:
        assertThat(isFamilyAssigned).isFalse();
    }

    @Test
    void givenAbsentAccount_whenAssignAccountToFamily_thenHandle404Response() {
        // Given:
        String missingAccountId = "fa63ec5b-38c7-4d11-befb-0227df4cad1b";
        String familyId = "f2dd6527-7d51-4f57-bc5d-f8cf8911dcf2";
        // When:
        Boolean isFamilyAssigned = accountManagementClient.setFamilyIdInAccountWithId(familyId, missingAccountId);
        // Then:
        assertThat(isFamilyAssigned).isFalse();
    }
}
