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
import org.springframework.web.client.HttpClientErrorException;
import pl.konradboniecki.budget.mvc.model.Jar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.budgetmanagement.JarManagementClientTest.*;

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
class JarManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "budget-management";
    public static final String STUB_VERSION = "0.7.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private JarManagementClient jarManagementClient;

    @BeforeEach
    void setUp() {
        jarManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenAbsentJarId_whenNotFoundInBudget_thenReturnEmpty() {
        // Given:
        String budgetId = "97f459b6-db3a-426a-9b3f-c40d589bc3a2";
        String absentJarId = "9f769ba3-8b72-4413-9709-f3c3394023eb";
        // When:
        Optional<Jar> jar = jarManagementClient.findInBudgetById(budgetId, absentJarId);
        // Then:
        assertThat(jar).isNotPresent();
    }

    @Test
    void givenPresentJarId_whenFoundInBudget_thenReturnJar() {
        // Given:
        String budgetId = "97f459b6-db3a-426a-9b3f-c40d589bc3a2";
        String presentJarId = "8514d8b8-9c87-4909-be0c-bb03c78c0819";
        // When:
        Optional<Jar> jarO = jarManagementClient.findInBudgetById(budgetId, presentJarId);
        // Then:
        assertThat(jarO).isPresent();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(jarO.get().getId()).isEqualTo(presentJarId),
                () -> assertThat(jarO.get().getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jarO.get().getJarName()).isNotEmpty(),
                () -> assertThat(jarO.get().getCurrentAmount()).isPositive(),
                () -> assertThat(jarO.get().getCapacity()).isPositive(),
                () -> assertThat(jarO.get().getStatus()).isEqualTo("IN PROGRESS")
        );
    }

    @Test
    void givenBudgetWithoutJars_whenJarsNotFound_thenReturnEmptyList() {
        // Given:
        String budgetWithoutJarsId = "25372644-0c05-4ca7-abda-5ec08f0391b3";
        // When:
        List<Jar> listOfJars = jarManagementClient.getAllJarsFromBudgetWithId(budgetWithoutJarsId);
        // Then:
        assertThat(listOfJars).isEmpty();
    }

    @Test
    void givenBudgetWithJars_whenJarsFound_thenReturnList() {
        // Given:
        String budgetWithJarsId = "bb973af2-1147-429b-9379-856a5ede2f60";
        // When:
        List<Jar> listOfJars = jarManagementClient.getAllJarsFromBudgetWithId(budgetWithJarsId);
        // Then:
        assertThat(listOfJars).isNotEmpty();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(listOfJars).hasSize(2),
                () -> assertThat(listOfJars.get(0).getId()).isEqualTo("afb2ab6f-7c0d-4ce7-8130-76efea5adc6b"),
                () -> assertThat(listOfJars.get(0).getBudgetId()).isEqualTo(budgetWithJarsId),
                () -> assertThat(listOfJars.get(0).getJarName()).isEqualTo("name1"),
                () -> assertThat(listOfJars.get(0).getCurrentAmount()).isZero(),
                () -> assertThat(listOfJars.get(0).getCapacity()).isEqualTo(3L),
                () -> assertThat(listOfJars.get(0).getStatus()).isEqualTo("NOT STARTED"),
                () -> assertThat(listOfJars.get(1).getId()).isEqualTo("b3e66a15-09e5-4a32-b9ec-d8c902bae0ea"),
                () -> assertThat(listOfJars.get(1).getBudgetId()).isEqualTo(budgetWithJarsId),
                () -> assertThat(listOfJars.get(1).getJarName()).isEqualTo("name2"),
                () -> assertThat(listOfJars.get(1).getCurrentAmount()).isZero(),
                () -> assertThat(listOfJars.get(1).getCapacity()).isEqualTo(3L),
                () -> assertThat(listOfJars.get(1).getStatus()).isEqualTo("NOT STARTED")
        );
    }

    @Test
    void givenPresentJar_whenDelete_thenReturnTrue() {
        // Given:
        String budgetId = "38410e86-5782-4390-b026-184558177c5f";
        String jarId = "666b975b-da45-4552-975b-26c559eb6b28";
        // When:
        Boolean result = jarManagementClient.removeJarFromBudget(jarId, budgetId);
        // Then:
        assertThat(result).isTrue();
    }

    @Test
    void givenAbsentJar_whenDelete_thenReturnFalse() {
        // Given:
        String budgetId = "38410e86-5782-4390-b026-184558177c5f";
        String absentJarId = "70fc6180-201e-4c7a-918e-095f8e9bfada";
        // When:
        Boolean result = jarManagementClient.removeJarFromBudget(absentJarId, budgetId);
        // Then:
        assertThat(result).isFalse();
    }

    @Test
    void givenInvalidBudgetIdInPathAndBody_whenSave_thenThrow() {
        // Given:
        String pathBudgetId = "ff2416e7-938e-4cfc-8419-96f5010d3a01";
        String payloadBudgetId = UUID.randomUUID().toString();
        Jar jarToSave = new Jar()
                .setBudgetId(payloadBudgetId)
                .setJarName("testJarName")
                .setCapacity(6L);
        // When:
        Throwable throwable = catchThrowable(
                () -> jarManagementClient.saveJar(jarToSave, pathBudgetId));
        // Then:
        assertThat(throwable).isNotNull()
                .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void givenValidJar_whenSave_thenReturnJar() {
        // Given:
        String budgetId = "39612072-c93d-4dc5-8eed-77b350c7533c";
        Jar jarToSave = new Jar()
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L);
        // When:
        Jar jar = jarManagementClient.saveJar(jarToSave, budgetId);
        // Then:
        assertThat(jar).isNotNull();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(UUID.fromString(jar.getId())).isNotNull(),
                () -> assertThat(jar.getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jar.getJarName()).isEqualTo("testJarName"),
                () -> assertThat(jar.getCapacity()).isEqualTo(6L),
                () -> assertThat(jar.getCurrentAmount()).isZero(),
                () -> assertThat(jar.getStatus()).isEqualTo("NOT STARTED")
        );
    }

    @Test
    void givenAbsentJar_whenUpdate_thenReturnEmpty() {
        // Given:
        String budgetId = "899a073e-12bf-4f27-85e4-3c004985e5b8";
        String absentJarId = "60b4ac5b-8b84-4eb5-a79a-b15af9d0761d";
        Jar jarToUpdate = new Jar()
                .setId(absentJarId)
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L)
                .setCurrentAmount(5L);
        // When:
        Optional<Jar> jarO = jarManagementClient.updateJar(jarToUpdate, budgetId);
        // Then:
        assertThat(jarO).isNotPresent();
    }

    @Test
    void givenPresentJar_whenUpdate_thenReturnJar() {
        // Given:
        String budgetId = "899a073e-12bf-4f27-85e4-3c004985e5b8";
        String presentJarId = "325c033d-f17d-48d2-b75a-e14458200704";
        Jar jarToUpdate = new Jar()
                .setId(presentJarId)
                .setBudgetId(budgetId)
                .setJarName("testJarName")
                .setCapacity(6L)
                .setCurrentAmount(5L);
        // When:
        Optional<Jar> jarO = jarManagementClient.updateJar(jarToUpdate, budgetId);
        // Then:
        assertThat(jarO).isPresent();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(jarO.get().getId()).isEqualTo(presentJarId),
                () -> assertThat(jarO.get().getBudgetId()).isEqualTo(budgetId),
                () -> assertThat(jarO.get().getJarName()).isEqualTo(jarToUpdate.getJarName()),
                () -> assertThat(jarO.get().getCapacity()).isEqualTo(jarToUpdate.getCapacity()),
                () -> assertThat(jarO.get().getCurrentAmount()).isEqualTo(jarToUpdate.getCurrentAmount()),
                () -> assertThat(jarO.get().getStatus()).isEqualTo("IN PROGRESS")
        );
    }
}
