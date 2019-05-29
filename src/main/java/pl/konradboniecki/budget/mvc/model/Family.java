package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.FamilyCreationForm;

@Data
@Accessors(chain = true)
public class Family {

    private String id;
    private String ownerId;
    private String budgetId;
    private String title;

    public Family() {
    }

    public Family(FamilyCreationForm familyCreationForm) {
        this();
        setTitle(familyCreationForm.getTitle());
    }

    public Family(FamilyCreationForm familyCreationForm, String ownerId) {
        this(familyCreationForm);
        setOwnerId(ownerId);
    }
}
