package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Jar;
import pl.konradboniecki.budget.openapi.dto.model.OASJar;
import pl.konradboniecki.budget.openapi.dto.model.OASJarPage;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class JarManagementClient {
    private static final String BASE_PATH = "/api/budget-mgt/v1";

    @Setter
    @Value("${budget.baseUrl.budgetManagement}")
    private String gatewayUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public JarManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Jar> findInBudgetById(String budgetId, String jarId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Jar> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/budgets/{budgetId}/jars/{jarId}",
                    HttpMethod.GET,
                    httpEntity, Jar.class, budgetId, jarId);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("jar with id: " + jarId + " not found in budget with id: " + budgetId);
            return Optional.empty();
        }
    }

    public List<Jar> getAllJarsFromBudgetWithId(String budgetId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<OASJarPage> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/budgets/{budgetId}/jars",
                    HttpMethod.GET,
                    httpEntity, OASJarPage.class, budgetId);
            return mapToJarList(responseEntity.getBody().getItems());
        } catch (HttpClientErrorException e) {
            log.error("error occured during fetch of all jars from budget with id: " + budgetId);
            return Collections.emptyList();
        }
    }

    private List<Jar> mapToJarList(List<OASJar> oasJarList) {
        return oasJarList.stream()
                .filter(Objects::nonNull)
                .map(oasJar ->
                        new Jar()
                                .setId(oasJar.getId())
                                .setBudgetId(oasJar.getBudgetId())
                                .setJarName(oasJar.getJarName())
                                .setCapacity(oasJar.getCapacity().longValue())
                                .setStatus(oasJar.getStatus())
                                .setCurrentAmount(oasJar.getCurrentAmount().longValue())
                )
                .collect(Collectors.toList());
    }

    public boolean removeJarFromBudget(String jarId, String budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/budgets/{budgetId}/jars/{jarId}",
                    HttpMethod.DELETE,
                    httpEntity, String.class, budgetId, jarId);
            return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Jar with id: " + jarId + " not found for budget with id: " + budgetId);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Failed to delete jar with id: " + jarId + ", from budget with id: " + budgetId + ".", e);
            return false;
        }
    }

    public Jar saveJar(Jar jar, String budgetId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Jar> httpEntity = new HttpEntity<>(jar, headers);

        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/budgets/{budgetId}/jars",
                    HttpMethod.POST,
                    httpEntity, Jar.class, budgetId);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Failed to create jar.", e);
            throw e;
        }
    }

    public Optional<Jar> updateJar(Jar jar, String budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Jar> httpEntity = new HttpEntity<>(jar, headers);
        String jarId = jar.getId();
        try {
            ResponseEntity<Jar> responseEntity = restTemplate.exchange(
                    gatewayUrl + BASE_PATH + "/budgets/{budgetId}/jars/{jarId}",
                    HttpMethod.PUT,
                    httpEntity, Jar.class, budgetId, jarId);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Failed to update jar in budget with id: " + budgetId, e);
            return Optional.empty();
        }
    }
}
