package pl.konradboniecki.budget.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import pl.konradboniecki.budget.mvc.service.ViewTemplate;

@Controller
public class ProfileController {

    @GetMapping("/budget/profile")
    public ModelAndView showBudgetHomePage() {
        return new ModelAndView(ViewTemplate.BUDGET_PROFILE_PAGE);
    }
}
