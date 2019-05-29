package pl.konradboniecki.budget.mvc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import pl.konradboniecki.budget.mvc.model.frontendforms.AccountForm;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class AccountTest {

    @Test
    void testIfHasFamily() {
        Account acc = new Account();
        assertFalse(acc.hasFamily());
        acc.setFamilyId(UUID.randomUUID().toString());
        assertTrue(acc.hasFamily());
    }

    @Test
    void testLowerCaseEmail(){
        Account acc = new Account();
        acc.setEmail("TEST@MAIL.com");
        assertEquals("test@mail.com", acc.getEmail());
    }

    @Test
    void testInitFromAccountForm(){
        AccountForm accForm = new AccountForm();
        accForm.setPassword("password");
        accForm.setEmail("TEST@mail.com");
        accForm.setRepeatedPassword("password");
        accForm.setFirstName("kon");
        accForm.setLastName("bon");

        Account acc = new Account(accForm);
        assertAll(
                () -> assertEquals(accForm.getEmail().toLowerCase(), acc.getEmail()),
                () -> assertEquals(accForm.getFirstName(), acc.getFirstName()),
                () -> assertEquals(accForm.getPassword(), acc.getPassword()),
                () -> assertNotNull(acc.getCreated()),
                () -> assertFalse(acc.isEnabled()),
                () -> assertNull(acc.getId()),
                () -> assertNull(acc.getFamilyId())
        );
    }
}
