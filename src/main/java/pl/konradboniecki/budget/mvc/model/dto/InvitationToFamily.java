package pl.konradboniecki.budget.mvc.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;

@Getter
@JsonDeserialize(builder = InvitationToFamily.InvitationToFamilyBuilder.class)
@Builder(builderClassName = "InvitationToFamilyBuilder", toBuilder = true)
public class InvitationToFamily {
   private final boolean guest;
   private final Account invitee;
   private final Account inviter;
   private final Family family;
   private final String invitationCode;
   private final String email;

   @JsonPOJOBuilder(withPrefix = "")
   public static class InvitationToFamilyBuilder {
   }
}
