package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Invitation {

    private String id;
    private String familyId;
    private String email;
    private String invitationCode;
    private Instant created;
    private Boolean registered;

    public Invitation(String email, String familyId) {
        setCreated(Instant.now());
        setEmail(email);
        setFamilyId(familyId);
    }

    public Invitation(String email, String familyId, String invitationCode, Boolean registered) {
        this(email, familyId);
        setInvitationCode(invitationCode);
        setRegistered(registered);
    }
}
