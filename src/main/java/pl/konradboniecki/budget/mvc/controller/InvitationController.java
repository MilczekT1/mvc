package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Invitation;
import pl.konradboniecki.budget.mvc.model.dto.InvitationToFamily;
import pl.konradboniecki.budget.mvc.service.SecurityHelper;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.FamilyManagementClient;
import pl.konradboniecki.budget.mvc.service.client.MailServiceClient;
import pl.konradboniecki.budget.mvc.service.client.accountmanagement.AccountManagementClient;

import java.util.Optional;
import java.util.UUID;

import static pl.konradboniecki.budget.mvc.service.ErrorType.ALREADY_IN_FAMILY;
import static pl.konradboniecki.budget.mvc.service.ErrorType.INVALID_INVITATION_LINK;

@Slf4j
@Controller
@RequestMapping(value = "/budget/family/invitations")
public class InvitationController {

    private AccountManagementClient accMgtClient;
    private MailServiceClient mailServiceClient;
    private FamilyManagementClient familyManagementClient;
    private SecurityHelper securityHelper;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public InvitationController(AccountManagementClient accMgtClient, MailServiceClient mailServiceClient,
                                FamilyManagementClient familyManagementClient, SecurityHelper securityHelper) {
        this.accMgtClient = accMgtClient;
        this.mailServiceClient = mailServiceClient;
        this.familyManagementClient = familyManagementClient;
        this.securityHelper = securityHelper;
    }

    @PostMapping("/invite-to-family")
    public ModelAndView handleInvitationToFamilyFromApp(@RequestParam("newMemberEmail") String newMemberEmail,
                                                        @ModelAttribute("familyObject") Family family) {
        String invitationCode = UUID.randomUUID().toString();
        boolean isNewUser = false;
        Optional<Account> newMember = accMgtClient.findAccountByEmail(newMemberEmail);
        if (newMember.isPresent()) {
            Optional<Account> owner = accMgtClient.findAccountById(family.getOwnerId());
            if (owner.isPresent()) {
                InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                        .guest(false)
                        .inviter(owner.get())
                        .invitee(newMember.get())
                        .family(family)
                        .invitationCode(invitationCode)
                        .build();
                mailServiceClient.sendFamilyInvitationToExistingUser(invitationToFamily);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found");
            }
        } else {
            // Invitation Code is not necessary
            isNewUser = true;
            String inviterEmail = SecurityContextHolder.getContext().getAuthentication().getName();//TODO: securityHelper.getEmailOfLoggedUser();
            Optional<Account> inviter = accMgtClient.findAccountByEmail(inviterEmail);
            if (inviter.isPresent()) {
                InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                        .guest(true)
                        .email(newMemberEmail)
                        .inviter(inviter.get())
                        .family(family)
                        .build();
                mailServiceClient.sendFamilyInvitationToNewUser(invitationToFamily);
            } else {
                log.error("Inviter has not been found: returning 500.");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.");
            }
        }

        Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(newMemberEmail, family.getId());
        if (invitation.isPresent()){
            familyManagementClient.deleteInvitationById(invitation.get().getId());
        }
        familyManagementClient.saveInvitation(new Invitation(newMemberEmail, family.getId(), invitationCode, isNewUser));

        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE, "familyObject", family);
    }

    @PostMapping("/invite-to-family/resend-invitation")
    public ModelAndView resendInvitationMail(@RequestParam("invitationId") String invitationId) {
        Optional<Invitation> invitation = familyManagementClient.findInvitationById(invitationId);
        if (invitation.isPresent()) {
            String emailDest = invitation.get().getEmail();
            Optional<Account> invitee = accMgtClient.findAccountByEmail(emailDest);
            Family family = familyManagementClient.findFamilyById(invitation.get().getFamilyId()).get();
            if (invitee.isPresent()) {
                Optional<Account> owner = accMgtClient.findAccountById(family.getOwnerId());
                InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                        .guest(false)
                        .inviter(owner.get())
                        .invitee(invitee.get())
                        .email(emailDest)
                        .family(family)
                        .invitationCode(invitation.get().getInvitationCode())
                        .build();
                mailServiceClient.sendFamilyInvitationToExistingUser(invitationToFamily);
            } else {
                String inviterEmail = SecurityContextHolder.getContext().getAuthentication().getName();//TODO: securityHelper.getEmailOfLoggedUser();
                Optional<Account> inviter = accMgtClient.findAccountByEmail(inviterEmail);
                if (inviter.isPresent()) {
                    InvitationToFamily invitationToFamily = InvitationToFamily.builder()
                            .guest(true)
                            .inviter(inviter.get())
                            .email(emailDest)
                            .family(family)
                            .build();
                    mailServiceClient.sendFamilyInvitationToNewUser(invitationToFamily);
                } else {
                    log.error("Inviter has not been found.");
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inviter has not been found.");
                }
            }
        }
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE);
    }

    @GetMapping("/{familyId}/addMember/{id}/{invitationCode}")
    public ModelAndView addAccountToFamily(@PathVariable("invitationCode") String code,
                                           @PathVariable("id") String accountId,
                                           @PathVariable("familyId") String familyId) {

        log.info("attempting to add member invitationCode: {}, accountId: {}, familyId: {}", code, accountId, familyId);
        Optional<Account> accountOpt = accMgtClient.findAccountById(accountId);
        if (familyManagementClient.findFamilyById(familyId).isPresent() &&
                accountOpt.isPresent()) {

            Account account = accountOpt.get();
            if (account.hasFamily()) {
                log.info("account already has a family: {}", account);
                return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", ALREADY_IN_FAMILY);
            } else {
                Optional<Invitation> invitation = familyManagementClient.findInvitationByEmailAndFamilyId(account.getEmail(), familyId);
                if (invitation.isPresent()) {
                    if (!invitation.get().getInvitationCode().equals(code)) {
                        log.error("Wrong invitation code: " + invitation.get().toString()
                                + "and given invitation code: " + code);
                        return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", INVALID_INVITATION_LINK);
                    } else {
                        accMgtClient.setFamilyIdInAccountWithId(familyId, accountId);
                        familyManagementClient.deleteInvitationById(invitation.get().getId());
                    }
                }
                else{
                    log.error("No such family invitation with  " + account.getEmail() + " and familyId:" + familyId);
                    return new ModelAndView(ViewTemplate.ERROR_PAGE, "errorType", INVALID_INVITATION_LINK);
                }
            }
        }
        return new ModelAndView("redirect:" + BASE_URL + "/login");
    }

    @PostMapping("/accept-invitation-in-family-creation-form")
    public ModelAndView acceptInvitationInFamilyCreationForm(
            @RequestParam(value = "familyOwnerId") String ownerId) {

        String inviteeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Account> invitee = accMgtClient.findAccountByEmail(inviteeEmail);
        Optional<Account> ownerOpt = accMgtClient.findAccountById(ownerId);
        String familyId = ownerOpt.get().getFamilyId();

        Optional<Invitation> invitationToDelete = familyManagementClient.findInvitationByEmailAndFamilyId(inviteeEmail, familyId);
        if (invitationToDelete.isPresent()) {
            familyManagementClient.deleteInvitationById(invitationToDelete.get().getId());
        }

        accMgtClient.setFamilyIdInAccountWithId(ownerOpt.get().getFamilyId(), invitee.get().getId());
        return new ModelAndView("redirect:" + BASE_URL + "/budget/family");
    }

    @PostMapping("/remove")
    public ModelAndView removeInvitation(@RequestParam("invitationId") String invitationId) {
        Optional<Invitation> familyInvitation = familyManagementClient.findInvitationById(invitationId);
        if (familyInvitation.isPresent()) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();// TODO: use security helper
            familyManagementClient.deleteInvitationById(invitationId);
            log.info("Invitation with id: " + invitationId + " to " + familyInvitation.get().getEmail()
                    + " has been deleted by: " + email);
        }
        return new ModelAndView("redirect:" + BASE_URL + "/" + ViewTemplate.FAMILY_HOME_PAGE);
    }
}
