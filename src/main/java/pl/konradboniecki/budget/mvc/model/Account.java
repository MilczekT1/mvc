package pl.konradboniecki.budget.mvc.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.AccountForm;

import java.time.Instant;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Account /*implements Serializable*/ {

    private String id;
    private String familyId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Instant created;
    private boolean enabled;

    public Account(AccountForm accountForm) {
        setFirstName(accountForm.getFirstName());
        setLastName(accountForm.getLastName());
        setEmail(accountForm.getEmail().toLowerCase());
        setPassword(accountForm.getPassword());
        created = Instant.now();
        setEnabled(false);
    }

    public Account(JsonNode jsonNode) {
        if (jsonNode.has("id")) {
            setId(jsonNode.path("id").asText());
        }
        if (jsonNode.has("familyId")) {
            setFamilyId(jsonNode.path("familyId").asText());
        }
        if (jsonNode.has("firstName")) setFirstName(jsonNode.path("firstName").asText());
        if (jsonNode.has("lastName")) setLastName(jsonNode.path("lastName").asText());
        if (jsonNode.has("email")) setEmail(jsonNode.path("email").asText());
        if (jsonNode.has("enabled")) setEnabled(jsonNode.path("enabled").asBoolean());
    }

    public Account setEmail(String email) {
        this.email = email.toLowerCase();
        return this;
    }

    public boolean hasFamily() {
        return familyId != null;
    }
}
