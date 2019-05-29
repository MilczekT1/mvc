package pl.konradboniecki.budget.mvc.service.client;

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
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.exceptions.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.mvc.service.client.FamilyManagementClientTest.*;

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
class FamilyManagementClientTest {
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "family-management";
    public static final String STUB_VERSION = "0.7.0-SNAPSHOT";

    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;
    @Autowired
    private FamilyManagementClient familyManagementClient;

    @BeforeEach
    void setUp() {
        familyManagementClient.setBASE_URL("http://localhost:" + stubRunnerPort);
    }

    @Test
    void givenOwnerId_whenFamilyFound_thenReturnFamily() {
        // Given:
        String ownerId = "82e84da2-83db-47d9-b8f1-df44f2971acb";
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByOwnerId(ownerId);
        // Then:
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(familyResponse.isPresent()).isTrue(),
                () -> assertThat(familyResponse.get().getId()).isNotNull(),
                () -> assertThat(familyResponse.get().getOwnerId()).isNotNull(),
                () -> assertThat(familyResponse.get().getOwnerId()).isEqualTo(ownerId),
                () -> assertThat(familyResponse.get().getBudgetId()).isNotNull(),
                () -> assertThat(familyResponse.get().getTitle()).isNotNull()
        );
    }

    @Test
    void givenOwnerId_whenFamilyNotFound_thenReturnEmpty() {
        // Given:
        String ownerId = "336dd389-2a7b-4360-8c13-607c8d126c4f";
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyByOwnerId(ownerId);
        // Then:
        assertThat(familyResponse.isPresent()).isFalse();
    }

    @Test
    void givenFamilyId_whenFamilyNotFound_thenReturnEmpty() {
        // Given:
        String familyId = "a31fed2a-8da7-46ea-8256-3b5e746d6fe5";
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyById(familyId);
        // Then:
        assertThat(familyResponse.isPresent()).isFalse();
    }

    @Test
    void givenFamilyId_whenFamilyFound_thenReturnFamily() {
        // Given:
        String familyId = "40320278-4656-4772-bd3a-68fd98ca5921";
        // When:
        Optional<Family> familyResponse = familyManagementClient.findFamilyById(familyId);
        // Then:
        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(familyResponse.isPresent()).isTrue(),
                () -> assertThat(familyResponse.get().getId()).isNotNull(),
                () -> assertThat(familyResponse.get().getId()).isEqualTo(familyId),
                () -> assertThat(familyResponse.get().getOwnerId()).isNotNull(),
                () -> assertThat(familyResponse.get().getBudgetId()).isNotNull(),
                () -> assertThat(familyResponse.get().getTitle()).isNotNull()
        );
    }

    @Test
    void givenExistingFamily_whenDelete_thenReturnTrue() {
        // Given:
        String familyId = "df511b31-0316-476e-8eff-f031692ac670";
        // When:
        boolean result = familyManagementClient.deleteFamilyById(familyId);
        // Then:
        assertThat(result).isTrue();
    }

    @Test
    void givenAbsentFamily_whenDelete_thenThrow() {
        // Given:
        String idOfMissingFamily = "a31fed2a-8da7-46ea-8256-3b5e746d6fe5";
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.deleteFamilyById(idOfMissingFamily));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenNewFamily_whenSave_thenReturnFamily() {
        // Given:
        Family family = new Family()
                .setOwnerId("37369461-0c5f-4240-8ff1-be20fda9c5df")
                .setBudgetId(UUID.randomUUID().toString())
                .setTitle("title");
        // When:
        Family createdFamily = familyManagementClient.saveFamily(family);
        // Then:
        Assertions.assertAll(
                () -> assertThat(createdFamily).isNotNull(),
                () -> assertThat(createdFamily.getOwnerId()).isEqualTo(family.getOwnerId()),
                () -> assertThat(UUID.fromString(createdFamily.getBudgetId())).isNotNull(),
                () -> assertThat(createdFamily.getTitle()).isEqualTo(family.getTitle()),
                () -> assertThat(createdFamily.getId()).isNotNull(),
                () -> assertThat(UUID.fromString(createdFamily.getId())).isNotNull()
        );
    }

    @Test
    void givenConflict_whenSave_thenThrow() {
        // Given:
        String idOfOwnerWhoAlreadyHasAFamily = "2015e088-f035-47be-b5cd-ae74d22c728d";
        Family familyToCreate = new Family()
                .setOwnerId(idOfOwnerWhoAlreadyHasAFamily)
                .setBudgetId(UUID.randomUUID().toString())
                .setTitle("testTitle");
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.saveFamily(familyToCreate));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void givenAbsentFamily_whenUpdate_thenThrow() {
        // Given:
        String invalidId = "eb44bc19-1b29-444e-bcd2-d9ef9a449bf0";
        Family familyWithInvalidId = new Family()
                .setId(invalidId)
                .setOwnerId(UUID.randomUUID().toString())
                .setBudgetId(UUID.randomUUID().toString())
                .setTitle("testTitle");
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.updateFamily(familyWithInvalidId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenPresentFamily_whenUpdate_thenReturnEditedFamily() {
        // Given:
        String familyId = "1dac6f28-9a85-407c-8e07-8371f9c2e5d9";
        Family familyBeforeEdit = new Family()
                .setId(familyId)
                .setBudgetId(UUID.randomUUID().toString());
        Family familyDuringEdit = new Family()
                .setId(familyBeforeEdit.getId())
                .setBudgetId(familyBeforeEdit.getBudgetId())
                .setTitle("newTitle")
                .setOwnerId(UUID.randomUUID().toString());
        // When:
        Family familyAfterEdit = familyManagementClient.updateFamily(familyDuringEdit);
        assertThat(familyAfterEdit).isEqualTo(familyDuringEdit);
        // Then:
        Assertions.assertAll(
                () -> assertThat(familyAfterEdit).isNotNull(),
                () -> assertThat(familyAfterEdit.getId()).isEqualTo(familyBeforeEdit.getId()),
                () -> assertThat(familyAfterEdit.getBudgetId()).isEqualTo(familyBeforeEdit.getBudgetId()),
                () -> assertThat(familyAfterEdit.getTitle()).isEqualTo(familyDuringEdit.getTitle()),
                () -> assertThat(familyAfterEdit.getOwnerId()).isEqualTo(familyDuringEdit.getOwnerId())
        );
    }

    @Test
    void givenPresentInvitation_whenDelete_thenReturnTrue() {
        // Given:
        String presentId = "a27e1edd-0a55-4531-9926-e74b95926174";
        // When:
        boolean fiDeleted = familyManagementClient.deleteInvitationById(presentId);
        // Then:
        assertThat(fiDeleted).isTrue();
    }

    @Test
    void givenAbsentInvitation_whenDelete_thenThrow() {
        // Given:
        String absentId = "0631f6c6-eb39-41dd-9895-da1a9258d3e4";
        // When:
        Throwable throwable = catchThrowable(
                () -> familyManagementClient.deleteInvitationById(absentId));
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void givenInvitation_whenSave_thenReturnInvitation() {
        // Given:
        Invitation invitationToSave = new Invitation()
                .setEmail("test@email.com")
                .setFamilyId("f75df896-c050-4b55-9950-5ce262925572")
                .setInvitationCode(UUID.randomUUID().toString())
                .setRegistered(false);
        // When:
        Invitation invitationAfterSave = familyManagementClient.saveInvitation(invitationToSave);
        // Then:
        Assertions.assertAll(
                () -> assertThat(invitationAfterSave).isNotNull(),
                () -> assertThat(invitationAfterSave.getId()).isNotNull(),
                () -> assertThat(UUID.fromString(invitationAfterSave.getId())).isNotNull(),
                () -> assertThat(invitationAfterSave.getEmail()).isEqualTo(invitationToSave.getEmail()),
                () -> assertThat(invitationAfterSave.getFamilyId()).isEqualTo(invitationToSave.getFamilyId()),
                () -> assertThat(invitationAfterSave.getInvitationCode()).isEqualTo(invitationToSave.getInvitationCode()),
                () -> assertThat(invitationAfterSave.getRegistered()).isEqualTo(invitationToSave.getRegistered())
        );
    }

    @Test
    void givenEmail_whenNoInvitations_thenReturnEmptyList() {
        // Given:
        String emailWithoutInvitations = "email@without-invitations.com";
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByEmail(emailWithoutInvitations);
        // Then:
        assertThat(invitations.size()).isEqualTo(0);
    }

    @Test
    void givenEmail_whenHasInvitations_thenReturnList() {
        // Given:
        String emailWithInvitations = "email@with-invitations.com";
        Invitation firstInvitation = new Invitation()
                .setId("90727450-8e56-4380-90c3-56fc56f4035d")
                .setFamilyId("2db9b8ca-2cc1-4129-b402-cfe75ca08547")
                .setEmail(emailWithInvitations)
                .setInvitationCode("34b7a194-b0d3-47f7-8aef-1d64caefcdf4")
                .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegistered(true);
        Invitation secondInvitation = new Invitation()
                .setId("7c28c191-821b-4977-996d-010787a203ee")
                .setFamilyId(null)
                .setEmail(emailWithInvitations)
                .setInvitationCode("c04a8005-cb67-46de-a4dc-e4f84d26faf3")
                .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegistered(true);
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByEmail(emailWithInvitations);
        // Then:
        assertThat(invitations.size()).isEqualTo(2);
        assertThat(invitations.contains(firstInvitation)).isTrue();
        assertThat(invitations.contains(secondInvitation)).isTrue();
    }

    @Test
    void givenFamilyId_whenNoInvitations_thenReturnEmptyList() {
        // Given:
        String familyId = "58701904-4407-4b26-ae77-745c61a49384";
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByFamilyId(familyId);
        // Then:
        assertThat(invitations.size()).isEqualTo(0);
    }

    @Test
    void givenFamilyId_whenHasInvitations_thenReturnList() {
        // Given:
        String familyId = "3ee03003-b049-47fe-9269-e64eaba640e7";
        Invitation firstInvitation = new Invitation()
                .setId("ff14b483-f28a-4b6a-a3ba-0d77de4b1682")
                .setFamilyId(familyId)
                .setEmail("mail_1@mail.com")
                .setInvitationCode("34b7a194-b0d3-47f7-8aef-1d64caefcdf4")
                .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegistered(true);
        Invitation secondInvitation = new Invitation()
                .setId("1474dffc-360a-4c33-a72d-c58fe72be1f2")
                .setFamilyId(familyId)
                .setEmail("mail_2@mail.com")
                .setInvitationCode("c04a8005-cb67-46de-a4dc-e4f84d26faf3")
                .setCreated(Instant.parse("2019-06-16T10:22:54.246625Z"))
                .setRegistered(false);
        // When:
        List<Invitation> invitations = familyManagementClient.findAllInvitationsByFamilyId(familyId);
        // Then:
        assertThat(invitations.size()).isEqualTo(2);
        assertThat(invitations.contains(firstInvitation)).isTrue();
        assertThat(invitations.contains(secondInvitation)).isTrue();
    }

    @Test
    public void givenEmailAndFamilyId_whenInvitationNotFound_thenReturnEmpty() {
        // Given:
        String familyId = "89f066a0-23a4-4e2a-aff5-7d7f920afa48";
        String email = "email@without-invitations.com";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(email, familyId);
        // Then:
        assertThat(invitation.isPresent()).isFalse();
    }

    @Test
    public void givenEmailAndFamilyId_whenInvitationFound_thenReturnInvitation() {
        // Given:
        String familyId = "091a6799-bce9-444d-982d-8724d4d31588";
        String email = "email@with-invitations.com";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(email, familyId);
        // Then:
        assertThat(invitation.isPresent()).isTrue();
        Assertions.assertAll(
                () -> assertThat(invitation.get().getId()).isNotNull(),
                () -> assertThat(invitation.get().getEmail()).isEqualTo(email),
                () -> assertThat(invitation.get().getFamilyId()).isEqualTo(familyId),
                () -> assertThat(invitation.get().getInvitationCode()).isNotNull(),
                () -> assertThat(invitation.get().getRegistered()).isNotNull(),
                () -> assertThat(invitation.get().getCreated()).isNotNull()
        );
    }

    @Test
    public void givenInvitationId_whenNotFound_thenReturnEmpty() {
        // Given:
        String id = "801bcae9-348a-4cd3-9793-7e6234461d5f";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationById(id);
        // Then:
        assertThat(invitation.isPresent()).isFalse();
    }

    @Test
    public void givenInvitationId_whenFound_thenReturnInvitation() {
        // Given:
        String id = "0728df5b-7f7f-42f9-8eae-251e86d8360a";
        // When:
        Optional<Invitation> invitation = familyManagementClient.findInvitationById(id);
        // Then:
        assertThat(invitation.isPresent()).isTrue();
        Assertions.assertAll(
                () -> assertThat(invitation.get().getId()).isNotNull(),
                () -> assertThat(UUID.fromString(invitation.get().getId())).isNotNull(),
                () -> assertThat(invitation.get().getEmail()).isNotNull(),
                () -> assertThat(invitation.get().getFamilyId()).isNotNull(),
                () -> assertThat(invitation.get().getInvitationCode()).isNotNull(),
                () -> assertThat(invitation.get().getRegistered()).isNotNull(),
                () -> assertThat(invitation.get().getCreated()).isNotNull()
        );
    }
}
