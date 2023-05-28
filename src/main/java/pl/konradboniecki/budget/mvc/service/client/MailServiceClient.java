package pl.konradboniecki.budget.mvc.service.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.dto.InvitationToFamily;
import pl.konradboniecki.budget.mvc.model.dto.SignUpConfirmation;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@Slf4j
@Service
public class MailServiceClient {
    private static final String BASE_PATH = "/api/mail/v1";

    private final RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.mail}")
    private String gatewayUrl;

    @Autowired
    public MailServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean sendSignUpConfirmation(Account account, String activationCode) throws HttpStatusCodeException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        SignUpConfirmation signUpConfirmation = new SignUpConfirmation()
                .setAccount(account)
                .setActivationCode(activationCode);
        HttpEntity<SignUpConfirmation> httpEntity = new HttpEntity<>(signUpConfirmation, headers);

        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                gatewayUrl + BASE_PATH + "/account-activations",
                HttpMethod.POST,
                httpEntity, Void.class);
        return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
    }

    public boolean sendFamilyInvitationToNewUser(InvitationToFamily invitationToFamily) {
        return sendFamilyInvitation(invitationToFamily);
    }

    public boolean sendFamilyInvitationToExistingUser(InvitationToFamily invitationToFamily) {
        return sendFamilyInvitation(invitationToFamily);
    }

    private boolean sendFamilyInvitation(InvitationToFamily invitationToFamily) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());

        HttpEntity<InvitationToFamily> httpEntity = new HttpEntity<>(invitationToFamily, headers);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                gatewayUrl + BASE_PATH + "/family-invitations",
                HttpMethod.POST,
                httpEntity, Void.class);
        return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
    }
}
