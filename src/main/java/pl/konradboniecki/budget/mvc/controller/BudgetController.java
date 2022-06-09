package pl.konradboniecki.budget.mvc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.model.Account;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.budget.mvc.model.Family;
import pl.konradboniecki.budget.mvc.model.Jar;
import pl.konradboniecki.budget.mvc.service.SecurityHelper;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;
import pl.konradboniecki.budget.mvc.service.client.FamilyManagementClient;
import pl.konradboniecki.budget.mvc.service.client.budgetmanagement.BudgetMgtServiceFacade;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = "/budget/manage")
public class BudgetController {

    private BudgetMgtServiceFacade budgetMgtServiceFacade;
    private FamilyManagementClient familyManagementClient;
    private SecurityHelper securityHelper;
    @Value("${budget.baseUrl.gateway}")
    private String gatewayUrl;

    @Autowired
    public BudgetController(BudgetMgtServiceFacade budgetMgtServiceFacade,
                            FamilyManagementClient familyManagementClient,
                            SecurityHelper securityHelper) {
        this.budgetMgtServiceFacade = budgetMgtServiceFacade;
        this.familyManagementClient = familyManagementClient;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ModelAndView showBudget(ModelMap modelMap) {
        Account acc = securityHelper.getLoggedAccountByEmail(securityHelper.getEmailOfLoggedUser());
        if (acc.getFamilyId() == null) {
            log.info("Family not found for email {}, redirecting to form ", acc.getEmail());
            return new ModelAndView("redirect:" + gatewayUrl + "/budget/family");
        } else {
            // TODO: Possible family absence
            Family family = familyManagementClient.findFamilyById(acc.getFamilyId()).orElseThrow();
            log.info("showing budget for family: {}", family);
            List<Jar> jarList = budgetMgtServiceFacade.getAllJarsFromBudgetWithId(family.getBudgetId());
            List<Expense> expenseList = budgetMgtServiceFacade.getAllExpensesFromBudgetWithId(family.getBudgetId());

            if (!jarList.isEmpty()) {
                modelMap.addAttribute("jarList", jarList);
            }
            if (!expenseList.isEmpty()) {
                modelMap.addAttribute("expenseList", expenseList);
            }
            modelMap.addAttribute("budgetId", family.getBudgetId());
            return new ModelAndView(ViewTemplate.BUDGET, modelMap);
        }
    }
}
