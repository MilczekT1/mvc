package pl.konradboniecki.budget.mvc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class InvitationTest {

    @Test
    void test() {
        String testMail = "test@mail.com";
        String testInvitCode = "abcdef";
        String testFamilyId = UUID.randomUUID().toString();

        Invitation familyInvitation1 = new Invitation();
        assertAll(
                () -> assertNull(familyInvitation1.getId()),
                () -> assertNull(familyInvitation1.getFamilyId()),
                () -> assertNull(familyInvitation1.getEmail()),
                () -> assertNull(familyInvitation1.getInvitationCode()),
                () -> assertNull(familyInvitation1.getCreated()),
                () -> assertNull(familyInvitation1.getRegistered())
        );
        Invitation familyInvitation2 = new Invitation(testMail, testFamilyId);
        assertAll(
                () -> assertNull(familyInvitation2.getId()),
                () -> assertEquals(testFamilyId, familyInvitation2.getFamilyId()),
                () -> assertEquals(testMail, familyInvitation2.getEmail()),
                () -> assertNull(familyInvitation2.getInvitationCode()),
                () -> assertNotNull(familyInvitation2.getCreated()),
                () -> assertNull(familyInvitation2.getRegistered())
        );
        Invitation familyInvitation3 = new Invitation(testMail,testFamilyId,testInvitCode,false);
        assertAll(
                () -> assertNull(familyInvitation3.getId()),
                () -> assertEquals(testFamilyId, familyInvitation3.getFamilyId()),
                () -> assertEquals(testMail, familyInvitation3.getEmail()),
                () -> assertEquals(testInvitCode, familyInvitation3.getInvitationCode()),
                () -> assertNotNull(familyInvitation3.getCreated()),
                () -> assertFalse(familyInvitation3.getRegistered())
        );
    }
}
