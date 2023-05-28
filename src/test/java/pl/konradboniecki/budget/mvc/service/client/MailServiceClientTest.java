package pl.konradboniecki.budget.mvc.service.client;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.dto.InvitationToFamily;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.MailServiceClientTest.*;

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
class MailServiceClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "mail";
    public static final String STUB_VERSION = "0.9.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private MailServiceClient mailServiceClient;

    @BeforeEach
    void setUp() {
        mailServiceClient.setGatewayUrl("http://localhost:" + stubRunnerPort);
    }


    @Test
    void sendSignUpConfirmation() {
        // Given:
        String activationCode = "29431ce1-8282-4489-8dd9-50f91e4c5653";
        Account acc = new Account()
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail("test@mail.com")
                .setId("bdde2539-37fd-4e06-897d-2c145ca4afba");
        // When:
        boolean isSent = mailServiceClient.sendSignUpConfirmation(acc, activationCode);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }

    @Test
    void inviteExistingUser() {
        // Given:
        Account invitee = new Account()
                .setId("63766b43-8eac-4719-b2e3-8165d1d3d077")
                .setFirstName("testFirstName1")
                .setLastName("testLastName1")
                .setEmail("email@email1.com");
        Account inviter = new Account()
                .setId("fc15fa5a-daf5-476d-aa22-404e7709d116")
                .setFirstName("testFirstName2")
                .setLastName("testLastName2")
                .setEmail("email@email2.com");
        Family family = new Family()
                .setId("eb75d4c2-534a-4c3e-84de-b4bec2d4bb36")
                .setTitle("testFamilyTitle");

        InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                .guest(false)
                .invitee(invitee)
                .inviter(inviter)
                .family(family)
                .invitationCode("8f37ab38-971a-471e-9fac-d8e63e47ce34")
                .build();
        // When:
        boolean isSent = mailServiceClient.sendFamilyInvitationToExistingUser(invitationToFamily);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }

    @Test
    void inviteNewUser() {
        // Given:
        Account inviter = new Account()
                .setId("5780007d-dcb2-42ab-9319-2695168336c0")
                .setFirstName("testFirstName2")
                .setLastName("testLastName2")
                .setEmail("email@email2.com");
        Family family = new Family()
                .setId("8b4c0235-f29a-4cf7-bdb3-505af01f4c7c")
                .setTitle("testFamilyTitle");

        InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                .guest(true)
                .email("test@mail.com")
                .inviter(inviter)
                .family(family)
                .build();
        // When:
        boolean isSent = mailServiceClient.sendFamilyInvitationToNewUser(invitationToFamily);
        // Then:
        Assertions.assertThat(isSent).isTrue();
    }
}
