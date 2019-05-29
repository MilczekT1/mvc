package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpense;
import pl.konradboniecki.budget.openapi.dto.model.OASExpensePage;
import pl.konradboniecki.chassis.exceptions.BadRequestException;
import pl.konradboniecki.chassis.exceptions.InternalServerErrorException;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static pl.konradboniecki.chassis.tools.RestTools.defaultGetHTTPHeaders;
import static pl.konradboniecki.chassis.tools.RestTools.defaultPostHTTPHeaders;

@Slf4j
@Service
public class ExpenseManagementClient {
    private static final String BASE_PATH = "/api/budget-mgt/v1";

    @Setter
    @Value("${budget.baseUrl.budgetManagement}")
    private String BASE_URL;
    private final RestTemplate restTemplate;

    @Autowired
    public ExpenseManagementClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Expense> getAllExpensesFromBudgetWithId(String budgetId) {
        HttpHeaders headers = defaultGetHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<OASExpensePage> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/budgets/{budgetId}/expenses",
                    HttpMethod.GET,
                    httpEntity, OASExpensePage.class, budgetId);
            return mapToExpenseList(responseEntity.getBody().getItems());
        } catch (HttpClientErrorException | NullPointerException e) {
            log.error("Error occurred during fetch of all expenses from budget with id: " + budgetId, e);
            return Collections.EMPTY_LIST;
        }
    }

    private List<Expense> mapToExpenseList(List<OASExpense> oasExpenseList) {
        return oasExpenseList.stream()
                .filter(Objects::nonNull)
                .map((oasExpense) ->
                        new Expense()
                                .setAmount(oasExpense.getAmount())
                                .setBudgetId(oasExpense.getBudgetId())
                                .setCreated(oasExpense.getCreated())
                                .setComment(oasExpense.getComment())
                                .setId(oasExpense.getId())
                )
                .collect(Collectors.toList());
    }

    public Expense saveExpense(Expense ex, String budgetId) {
        HttpHeaders headers = defaultPostHTTPHeaders();
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<Expense> httpEntity = new HttpEntity<>(ex, headers);
        try {
            ResponseEntity<Expense> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH + "/budgets/{budgetId}/expenses",
                    HttpMethod.POST,
                    httpEntity, Expense.class, budgetId);
            return responseEntity.getBody();
        } catch (HttpServerErrorException e) {
            log.error("Failed to save expense.", e);
            throw new InternalServerErrorException("Failed to save expense.", e);
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Failed to save expense.", e);
            throw new BadRequestException("Failed to save expense.");
        }
    }

    public boolean deleteExpenseInBudget(String expenseId, String budgetId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    BASE_URL + BASE_PATH +"/budgets/{budgetId}/expenses/{expenseId}",
                    HttpMethod.DELETE,
                    httpEntity, String.class, budgetId, expenseId);
            return responseEntity.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Expense with id: " + expenseId + " not found in budget with id: " + budgetId);
            return false;
        } catch (HttpClientErrorException e) {
            log.error("Failed to remove expense With id: " + expenseId + "from budget with id: " + budgetId, e);
            return false;
        }
    }
}
