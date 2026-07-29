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
    private final Checkbox countdownToggle = new Checkbox("Show Due Time as Countdown");
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

        dueDate.setMin(LocalDate.now());
        dueDate.setEnabled(false);
        dueDate.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().isBefore(LocalDate.now())) {
                dueDate.setValue(LocalDate.now());
                Notification.show("Due date cannot be in the past!");
            }
        });
        dueTime.setEnabled(false);
        countdownToggle.setEnabled(false);
        dueDateToggle.addValueChangeListener(e -> {
            boolean enabled = e.getValue();
            dueDate.setEnabled(enabled);
            dueTime.setEnabled(enabled);
            countdownToggle.setEnabled(enabled);
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

        HorizontalLayout dueToggleLayout = new HorizontalLayout(dueDateToggle, countdownToggle);
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
        if (dueDateToggle.getValue()) {
            if (dueDate.getValue() == null) {
                Notification.show("Please select a due date!");
                return;
            }
            LocalDate selectedDate = dueDate.getValue();
            LocalTime selectedTime = dueTime.getValue() != null ? dueTime.getValue() : LocalTime.of(23, 59);

            java.time.LocalDateTime selectedDateTime = java.time.LocalDateTime.of(selectedDate, selectedTime);
            if (selectedDateTime.isBefore(java.time.LocalDateTime.now())) {
                Notification.show("Due date and time cannot be in the past!");
                return;
            }

            dateStr = selectedDate.toString();
            timeStr = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        String hl = hyperlinkToggle.getValue() ? hyperlinkInput.getValue() : "";

        WorkItem newItem = new WorkItem();
        newItem.setTitle(workInput.getValue());
        newItem.setCategory(category);
        newItem.setDueDate(dateStr);
        newItem.setDueTime(timeStr);
        newItem.setHyperlink(hl);
        newItem.setDescription(descriptionInput.getValue());
        newItem.setCountdown(dueDateToggle.getValue() && countdownToggle.getValue());

        workService.addWork(newItem);

        Notification.show("Work item '" + newItem.getId() + "' added successfully!");
        UI.getCurrent().navigate(WorkListView.class);
    }
}
