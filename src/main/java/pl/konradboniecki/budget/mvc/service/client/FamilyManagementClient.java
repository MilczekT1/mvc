package pl.konradboniecki.budget.mvc.service.client;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.budget.openapi.dto.model.OASInvitation;
import pl.konradboniecki.budget.openapi.dto.model.OASInvitationPage;
import pl.konradboniecki.chassis.exceptions.BadRequestException;
import pl.konradboniecki.chassis.exceptions.ResourceConflictException;
import pl.konradboniecki.chassis.exceptions.ResourceNotFoundException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class FamilyManagementClient {
    private static final String BASE_PATH = "/api/family-mgt/v1";

    private final RestTemplate restTemplate;
    @Setter
    @Value("${budget.baseUrl.familyManagement}")
    private String BASE_URL;

    @Autowired
    public FamilyManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<Family> findFamilyById(String familyId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity httpEntity = new HttpEntity(headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/families/{familyId}",
                    HttpMethod.GET,
                    httpEntity, Family.class, familyId);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error("Family with id: " + familyId + " not found.");
            return Optional.empty();
        }
    }

    @Deprecated(forRemoval = true)
    public Optional<Family> findFamilyByOwnerId(String ownerId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/families/owners/{ownerId}",
                    HttpMethod.GET,
                    httpEntity, Family.class, ownerId);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error("Family with ownerId: " + ownerId + " not found.");
            return Optional.empty();
        }
    }


    public boolean deleteFamilyById(String familyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/families/" + familyId,
                    HttpMethod.DELETE,
                    httpEntity, Void.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            throw new ResourceNotFoundException("Failed to delete family with id: " + familyId, e);
        }
    }

    public Family saveFamily(Family family) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Family> httpEntity = new HttpEntity<>(family, headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/families",
                    HttpMethod.POST,
                    httpEntity, Family.class);
            return responseEntity.getBody();
        } catch (HttpClientErrorException.Conflict e) {
            log.error("Failed to save app with id: {}, already exists.", family.getId());
            throw new ResourceConflictException("Conflict during family creation. Conflict.", e);
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to create family.", e);
        }
    }

    public Family updateFamily(Family family) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(family, headers);
        try {
            ResponseEntity<Family> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/families/{familyId}",
                    HttpMethod.PUT,
                    httpEntity, Family.class, family.getId());
            return responseEntity.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Family not found with id: " + family.getId(), e);
        }
    }

    public boolean deleteInvitationById(String invitationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/invitations/{invitationId}",
                    HttpMethod.DELETE,
                    httpEntity, Void.class, invitationId);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Family with id: " + invitationId + " not found.", e);
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to delete family invitation", e);
        }
    }

    public Invitation saveInvitation(Invitation invitation) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Invitation> httpEntity = new HttpEntity<>(invitation, headers);
        log.info("Saving invitation with following body: {}", httpEntity.getBody().toString());
        ResponseEntity<Invitation> responseEntity = restTemplate.exchange(
                BASE_URL + BASE_PATH + "/invitations",
                HttpMethod.POST,
                httpEntity, Invitation.class);
        return responseEntity.getBody();
    }

    public List<Invitation> findAllInvitationsByEmail(String email) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<OASInvitationPage> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/invitations?email={email}",
                    HttpMethod.GET,
                    httpEntity, OASInvitationPage.class, email);
            return mapToInvitationList(responseEntity.getBody().getItems());
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch all invitations for email: " + email);
            return Collections.emptyList();
        }
    }

    private List<Invitation> mapToInvitationList(List<OASInvitation> oasInvitation) {
        return oasInvitation.stream()
                .filter(Objects::nonNull)
                .map((oasInvitation1) ->
                    new Invitation()
                            .setId(oasInvitation1.getId())
                            .setFamilyId(oasInvitation1.getFamilyId())
                            .setEmail(oasInvitation1.getEmail())
                            .setInvitationCode(oasInvitation1.getInvitationCode())
                            .setCreated(oasInvitation1.getCreated())
                            .setRegistered(oasInvitation1.getRegistered())
                )
                .collect(Collectors.toList());
    }

    public List<Invitation> findAllInvitationsByFamilyId(String id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<OASInvitationPage> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/invitations?familyId=" + id,
                    HttpMethod.GET,
                    httpEntity, OASInvitationPage.class);
            return mapToInvitationList(responseEntity.getBody().getItems());
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch all invitations to family with id: " + id, e);
            return Collections.emptyList();
        }
    }

    public Optional<Invitation> findInvitationByEmailAndFamilyId(String email, String familyId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<OASInvitationPage> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/invitations?email={email}&familyId={familyId}",
                    HttpMethod.GET, httpEntity,
                    OASInvitationPage.class, email, familyId);
            return mapToInvitationList(responseEntity.getBody().getItems())
                    .stream()
                    .findFirst();
        } catch (HttpClientErrorException e) {
            log.error("FamilyInvitation with email: {} and id: {} not found.", email, familyId, e);
            return Optional.empty();
        }
    }

    public Optional<Invitation> findInvitationById(String id) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Invitation> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/invitations/" + id,
                    HttpMethod.GET,
                    httpEntity, Invitation.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            log.error("FamilyInvitation with id:" + id + " not found.", e);
            return Optional.empty();
        }
    }
}
