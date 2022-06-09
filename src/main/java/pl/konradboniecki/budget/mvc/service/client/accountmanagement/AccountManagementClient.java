package pl.konradboniecki.budget.mvc.service.client.accountmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Optional;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class AccountManagementClient {
    private static final String BASE_PATH = "/api/account-mgt/v1";

    private final RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.accountManagement}")
    private String gatewayUrl;

    @Autowired
    public AccountManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Account> findAccountById(String id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Account> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts/" + id + "?findBy=id",
                    HttpMethod.GET,
                    httpEntity, Account.class);
            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Account with id: " + id + " not found.", e);
            return Optional.empty();
        }
    }

    public Optional<Account> findAccountByEmail(String email) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Account> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts/" + email + "?findBy=email",
                    HttpMethod.GET,
                    httpEntity, Account.class);
            return Optional.of(responseEntity.getBody());
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Account with email: " + email + " not found.", e);
            return Optional.empty();
        }
    }

    public Account saveAccount(Account accountToSave) throws ResourceConflictException {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(accountToSave, headers);
        try {
            ResponseEntity<Account> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts",
                    HttpMethod.POST,
                    httpEntity, Account.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to create account with email: " + accountToSave.getEmail());
            throw e;
        }
    }

    public String createActivationCodeForAccount(String accountId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.set("id", accountId);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts/{accountId}/activation-codes",
                    HttpMethod.POST,
                    httpEntity, JsonNode.class, accountId);
            return responseEntity.getBody().path("activationCode").asText();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Error during activation code creation. Account with id {} not found.", accountId);
            throw e;
        }
    }

    public Boolean checkIfPasswordIsCorrect(String accountId, String hashedPassword) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.set("password", hashedPassword);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts/{accountId}/credentials",
                    HttpMethod.GET,
                    httpEntity, Void.class, accountId);
            return responseEntity.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.BadRequest e) {
            log.info("Password not matched for account with id: " + accountId, e);
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Account with id: " + accountId + " not found during password check", e);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Failed to validate password, check failed.", e);
            return false;
        }
    }

    public Boolean setFamilyIdInAccountWithId(String familyId, String accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/accounts/{accountId}/families/{familyId}",
                    HttpMethod.PUT,
                    httpEntity, String.class, accountId, familyId);
            return responseEntity.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            if (e instanceof HttpClientErrorException.NotFound) {
                log.info("Account with id: {} or family with id: {} not found.", accountId, familyId);
            }
            return false;
        }
    }
}
