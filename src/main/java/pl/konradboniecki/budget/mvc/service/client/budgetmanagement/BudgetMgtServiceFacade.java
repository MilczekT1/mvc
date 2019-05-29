package pl.konradboniecki.budget.mvc.service.client.budgetmanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.mvc.model.Budget;
import pl.konradboniecki.budget.mvc.model.Expense;
import pl.konradboniecki.budget.mvc.model.Jar;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BudgetMgtServiceFacade {

    private BudgetManagementClient budgetManagementClient;
    private ExpenseManagementClient expenseManagementClient;
    private JarManagementClient jarManagementClient;

    @Autowired
    public BudgetMgtServiceFacade(BudgetManagementClient budgetManagementClient,
                                  ExpenseManagementClient expenseManagementClient,
                                  JarManagementClient jarManagementClient) {
        this.budgetManagementClient = budgetManagementClient;
        this.expenseManagementClient = expenseManagementClient;
        this.jarManagementClient = jarManagementClient;
    }

    public Optional<Budget> findBudgetByFamilyId(String familyId) {
        return budgetManagementClient.findBudgetByFamilyId(familyId);
    }

    public Budget saveBudget(Budget budget) {
        return budgetManagementClient.saveBudget(budget);
    }

    public Optional<Jar> findJarByIdInBudget(String budgetId, String jarId) {
        return jarManagementClient.findInBudgetById(budgetId, jarId);
    }

    public List<Jar> getAllJarsFromBudgetWithId(String budgetId) {
        return jarManagementClient.getAllJarsFromBudgetWithId(budgetId);
    }

    public boolean removeJarFromBudget(String jarId, String budgetId) {
        return jarManagementClient.removeJarFromBudget(jarId, budgetId);
    }

    public Jar saveJar(Jar jar, String budgetId) {
        return jarManagementClient.saveJar(jar, budgetId);
    }

    public Optional<Jar> updateJar(Jar jar, String budgetId) {
        return jarManagementClient.updateJar(jar, budgetId);
    }

    public List<Expense> getAllExpensesFromBudgetWithId(String budgetId) {
        return expenseManagementClient.getAllExpensesFromBudgetWithId(budgetId);
    }

    public Expense saveExpense(Expense ex, String budgetId) {
        return expenseManagementClient.saveExpense(ex, budgetId);
    }

    public boolean deleteExpenseInBudget(String expenseId, String budgetId) {
        return expenseManagementClient.deleteExpenseInBudget(expenseId, budgetId);
    }
}
