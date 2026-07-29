package mipax.workrewards.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import mipax.workrewards.model.WorkItem;
import mipax.workrewards.service.WorkService;
import mipax.workrewards.ui.MainLayout;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@PageTitle("Add Work | Work Rewards")
@Route(value = "add", layout = MainLayout.class)
public class AddWorkView extends VerticalLayout {

    private final WorkService workService;

    private final TextField workInput = new TextField("Work Title");
    private final ComboBox<String> categoryComboBox = new ComboBox<>("Category");
    private final Checkbox dueDateToggle = new Checkbox("Enable Due Date");
    private final DatePicker dueDate = new DatePicker("Due Date");
    private final TimePicker dueTime = new TimePicker("Due Time");

    private final Checkbox hyperlinkToggle = new Checkbox("Enable Hyperlink");
    private final TextField hyperlinkInput = new TextField("Hyperlink");

    private final TextArea descriptionInput = new TextArea("Description");

    public AddWorkView(WorkService workService) {
        this.workService = workService;
        setSizeFull();
        setMaxWidth("800px");
        setPadding(true);

        add(new H2("Add New Work Item"));

        configureFields();
        createForm();
    }

    private void configureFields() {
        workInput.setPlaceholder("Work to do...");
        workInput.setRequired(true);
        workInput.setWidthFull();

        categoryComboBox.setItems(workService.getCategories());
        categoryComboBox.setValue("Main");
        categoryComboBox.setAllowCustomValue(true);
        categoryComboBox.addCustomValueSetListener(e -> {
            String customCat = e.getDetail();
            categoryComboBox.setValue(customCat);
        });

        dueDate.setEnabled(false);
        dueTime.setEnabled(false);
        dueDateToggle.addValueChangeListener(e -> {
            boolean enabled = e.getValue();
            dueDate.setEnabled(enabled);
            dueTime.setEnabled(enabled);
            if (enabled && dueDate.getValue() == null) {
                dueDate.setValue(LocalDate.now());
            }
            if (enabled && dueTime.getValue() == null) {
                dueTime.setValue(LocalTime.of(12, 0));
            }
        });

        hyperlinkInput.setEnabled(false);
        hyperlinkToggle.addValueChangeListener(e -> hyperlinkInput.setEnabled(e.getValue()));

        descriptionInput.setPlaceholder("Detailed notes or requirements...");
        descriptionInput.setWidthFull();
        descriptionInput.setHeight("150px");
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        HorizontalLayout dueToggleLayout = new HorizontalLayout(dueDateToggle);
        HorizontalLayout dueInputLayout = new HorizontalLayout(dueDate, dueTime);

        HorizontalLayout hlToggleLayout = new HorizontalLayout(hyperlinkToggle);

        formLayout.add(workInput, categoryComboBox);
        formLayout.add(dueToggleLayout, hlToggleLayout);
        formLayout.add(dueInputLayout, hyperlinkInput);
        formLayout.add(descriptionInput, 2);

        Button saveButton = new Button("Add Work", e -> saveWork());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(WorkListView.class));

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        add(formLayout, buttonLayout);
    }

    private void saveWork() {
        if (workInput.isEmpty()) {
            Notification.show("Please enter a title for the work item!");
            return;
        }

        String category = categoryComboBox.getValue();
        if (category == null || category.trim().isEmpty()) {
            category = "Main";
        }

        String dateStr = "00-00-00";
        String timeStr = "00:00";
        if (dueDateToggle.getValue() && dueDate.getValue() != null) {
            dateStr = dueDate.getValue().toString();
        }
        if (dueDateToggle.getValue() && dueTime.getValue() != null) {
            timeStr = dueTime.getValue().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        String hl = hyperlinkToggle.getValue() ? hyperlinkInput.getValue() : "";

        WorkItem newItem = new WorkItem();
        newItem.setTitle(workInput.getValue());
        newItem.setCategory(category);
        newItem.setDueDate(dateStr);
        newItem.setDueTime(timeStr);
        newItem.setHyperlink(hl);
        newItem.setDescription(descriptionInput.getValue());

        workService.addWork(newItem);

        Notification.show("Work item '" + newItem.getId() + "' added successfully!");
        UI.getCurrent().navigate(WorkListView.class);
    }
}
