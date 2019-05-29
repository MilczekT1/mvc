package pl.konradboniecki.budget.mvc.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.Account;

@Data
@Accessors(chain = true)
public class SignUpConfirmation {
    private Account account;
    private String activationCode;
}
