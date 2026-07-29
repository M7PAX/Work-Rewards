package mipax.workrewards.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import mipax.workrewards.model.WorkItem;
import mipax.workrewards.service.WorkService;
import mipax.workrewards.ui.MainLayout;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@PageTitle("Work List | Work Rewards")
@Route(value = "", layout = MainLayout.class)
public class WorkListView extends VerticalLayout {

    private final WorkService workService;

    private final VerticalLayout cardContainer = new VerticalLayout();
    private final TextField searchField = new TextField();
    private final Consumer<String> categoryListener = cat -> updateGridItems();

    public WorkListView(WorkService workService) {
        this.workService = workService;
        setSizeFull();
        setPadding(true);

        createHeader();
        createFilterToolbar();

        cardContainer.setWidthFull();
        cardContainer.setPadding(false);
        cardContainer.setSpacing(true);

        add(cardContainer);
        updateView();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        MainLayout.addCategoryChangeListener(categoryListener);
        UI ui = attachEvent.getUI();
        ui.setPollInterval(2000);
        ui.addPollListener(e -> updateView());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        MainLayout.removeCategoryChangeListener(categoryListener);
        getUI().ifPresent(ui -> ui.setPollInterval(-1));
    }

    private void createHeader() {
        H2 header = new H2("Work Rewards Overview");
        header.getStyle().set("margin-top", "0");
        add(header);
    }

    private void createFilterToolbar() {
        searchField.setPlaceholder("Search work title or description...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGridItems());
        searchField.setWidthFull();

        HorizontalLayout toolbar = new HorizontalLayout(searchField);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();

        add(toolbar);
    }

    private HorizontalLayout createTaskCard(WorkItem item) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("task-card");
        card.setWidthFull();
        card.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Checkbox doneBox = new Checkbox(item.isCompleted());
        doneBox.getStyle().set("width", "36px").set("flex-shrink", "0");
        doneBox.addValueChangeListener(e -> {
            item.setCompleted(e.getValue());
            workService.updateItem(item);
            if (e.getValue()) {
                Notification.show("Marked as Completed: " + item.getTitle());
            } else {
                Notification.show("Reopened Task: " + item.getTitle());
            }
            updateView();
        });

        Span catBadge = new Span(item.getCategory() != null ? item.getCategory() : "Main");
        catBadge.getStyle()
                .set("background", "var(--lumo-contrast-10pct)")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("width", "90px")
                .set("min-width", "90px")
                .set("text-align", "center")
                .set("flex-shrink", "0");

        VerticalLayout textContainer = new VerticalLayout();
        textContainer.setPadding(false);
        textContainer.setSpacing(false);
        textContainer.getStyle().set("overflow", "hidden");

        Span titleSpan = new Span(item.getTitle());
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        Span descSpan = new Span(item.getDescription() != null ? item.getDescription() : "");
        descSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        textContainer.add(titleSpan, descSpan);

        Span dueBadge = new Span(formatDueDateOrCountdown(item));
        dueBadge.getStyle()
                .set("cursor", "pointer")
                .set("padding", "6px 12px")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500")
                .set("width", "160px")
                .set("min-width", "160px")
                .set("text-align", "center")
                .set("margin-right", "12px")
                .set("flex-shrink", "0");
        dueBadge.addClickListener(e -> {
            item.setCountdown(!item.isCountdown());
            workService.updateItem(item);
            updateView();
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        actions.getStyle()
                .set("min-width", "200px")
                .set("justify-content", "flex-end")
                .set("gap", "4px")
                .set("flex-shrink", "0");

        if (item.getHyperlink() != null && !item.getHyperlink().trim().isEmpty()) {
            Anchor link = new Anchor(item.getHyperlink(), VaadinIcon.EXTERNAL_LINK.create());
            link.setTarget("_blank");
            actions.add(link);
        }

        Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> showEditDialog(item));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editBtn.setTooltipText("Edit Task");

        Button detailsBtn = new Button(VaadinIcon.INFO_CIRCLE.create(), e -> showDetailsDialog(item));
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
            workService.deleteWork(item.getId());
            Notification.show("Deleted task");
            updateView();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        actions.add(editBtn, detailsBtn, deleteBtn);

        card.add(doneBox, catBadge, textContainer, dueBadge, actions);
        card.setFlexGrow(1, textContainer);

        return card;
    }

    private void showEditDialog(WorkItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Work Item");

        TextField titleInput = new TextField("Work Title");
        titleInput.setValue(item.getTitle() != null ? item.getTitle() : "");
        titleInput.setWidthFull();

        ComboBox<String> categoryCombo = new ComboBox<>("Category");
        categoryCombo.setItems(workService.getCategories());
        categoryCombo.setValue(item.getCategory() != null ? item.getCategory() : "Main");
        categoryCombo.setAllowCustomValue(true);
        categoryCombo.addCustomValueSetListener(e -> categoryCombo.setValue(e.getDetail()));

        Checkbox dueToggle = new Checkbox("Enable Due Date");
        Checkbox countdownBox = new Checkbox("Show Due Time as Countdown");
        DatePicker datePicker = new DatePicker("Due Date");
        TimePicker timePicker = new TimePicker("Due Time");
        datePicker.setMin(LocalDate.now());

        boolean hasDue = item.getDueDate() != null && !item.getDueDate().isEmpty() && !"00-00-00".equals(item.getDueDate());
        dueToggle.setValue(hasDue);
        datePicker.setEnabled(hasDue);
        timePicker.setEnabled(hasDue);
        countdownBox.setEnabled(hasDue);
        countdownBox.setValue(item.isCountdown());

        if (hasDue) {
            try {
                datePicker.setValue(LocalDate.parse(item.getDueDate()));
            } catch (Exception ignored) {}
            try {
                timePicker.setValue(LocalTime.parse(item.getDueTime()));
            } catch (Exception ignored) {}
        }

        dueToggle.addValueChangeListener(e -> {
            boolean enabled = e.getValue();
            datePicker.setEnabled(enabled);
            timePicker.setEnabled(enabled);
            countdownBox.setEnabled(enabled);
        });

        TextField hlInput = new TextField("Hyperlink");
        hlInput.setValue(item.getHyperlink() != null ? item.getHyperlink() : "");
        hlInput.setWidthFull();

        TextArea descInput = new TextArea("Description");
        descInput.setValue(item.getDescription() != null ? item.getDescription() : "");
        descInput.setWidthFull();

        FormLayout formLayout = new FormLayout();
        formLayout.add(titleInput, categoryCombo);
        formLayout.add(new HorizontalLayout(dueToggle, countdownBox));
        formLayout.add(new HorizontalLayout(datePicker, timePicker), hlInput);
        formLayout.add(descInput, 2);

        Button saveBtn = new Button("Save Changes", e -> {
            if (titleInput.isEmpty()) {
                Notification.show("Please enter a title");
                return;
            }
            item.setTitle(titleInput.getValue());
            item.setCategory(categoryCombo.getValue() != null ? categoryCombo.getValue() : "Main");
            item.setHyperlink(hlInput.getValue());
            item.setDescription(descInput.getValue());

            if (dueToggle.getValue()) {
                if (datePicker.getValue() == null) {
                    Notification.show("Please select a due date");
                    return;
                }
                LocalDate sDate = datePicker.getValue();
                LocalTime sTime = timePicker.getValue() != null ? timePicker.getValue() : LocalTime.of(23, 59);
                LocalDateTime sDateTime = LocalDateTime.of(sDate, sTime);
                if (sDateTime.isBefore(LocalDateTime.now())) {
                    Notification.show("Due date and time cannot be in the past!");
                    return;
                }
                item.setDueDate(sDate.toString());
                item.setDueTime(sTime.format(DateTimeFormatter.ofPattern("HH:mm")));
                item.setCountdown(countdownBox.getValue());
            } else {
                item.setDueDate("00-00-00");
                item.setDueTime("00:00");
                item.setCountdown(false);
            }

            workService.updateItem(item);
            Notification.show("Task updated successfully!");
            dialog.close();
            updateView();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(saveBtn, cancelBtn);
        dialog.open();
    }

    private void showDetailsDialog(WorkItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Work Details");

        VerticalLayout content = new VerticalLayout();
        content.add(new Span("Title: " + item.getTitle()));
        content.add(new Span("Category: " + item.getCategory()));
        content.add(new Span("Due: " + formatDueDateOrCountdown(item)));
        content.add(new Span("Description: " + item.getDescription()));
        if (item.getHyperlink() != null && !item.getHyperlink().isEmpty()) {
            content.add(new Anchor(item.getHyperlink(), "Open Hyperlink: " + item.getHyperlink()));
        }

        Button close = new Button("Close", e -> dialog.close());
        dialog.add(content);
        dialog.getFooter().add(close);
        dialog.open();
    }

    private void updateView() {
        workService.load();
        updateGridItems();
    }

    private void updateGridItems() {
        cardContainer.removeAll();
        List<WorkItem> items = workService.getAllItems();
        String filterText = searchField.getValue() != null ? searchField.getValue().toLowerCase().trim() : "";
        String selectedCat = MainLayout.getSelectedCategory();

        List<WorkItem> filtered = items.stream()
                .filter(item -> !item.isCompleted())
                .filter(item -> {
                    boolean matchesText = filterText.isEmpty()
                            || (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterText))
                            || (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterText));
                    boolean matchesCat = selectedCat == null || "All".equalsIgnoreCase(selectedCat)
                            || selectedCat.equalsIgnoreCase(item.getCategory());
                    return matchesText && matchesCat;
                })
                .sorted((a, b) -> {
                    LocalDateTime timeA = parseDueDateTime(a);
                    LocalDateTime timeB = parseDueDateTime(b);
                    if (timeA == null && timeB == null) return 0;
                    if (timeA == null) return 1;
                    if (timeB == null) return -1;
                    return timeA.compareTo(timeB);
                })
                .collect(Collectors.toList());

        for (WorkItem item : filtered) {
            cardContainer.add(createTaskCard(item));
        }
    }

    private static LocalDateTime parseDueDateTime(WorkItem item) {
        if (item.getDueDate() == null || item.getDueDate().isEmpty() || "00-00-00".equals(item.getDueDate())) {
            return null;
        }
        String timeStr = (item.getDueTime() != null && !item.getDueTime().isEmpty()) ? item.getDueTime() : "00:00";
        try {
            return LocalDateTime.parse(item.getDueDate() + " " + timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDueDateOrCountdown(WorkItem item) {
        if (item.getDueDate() == null || item.getDueDate().isEmpty() || "00-00-00".equals(item.getDueDate())) {
            return "No Due Date";
        }
        String timeStr = (item.getDueTime() != null && !item.getDueTime().isEmpty()) ? item.getDueTime() : "00:00";
        String fullStr = item.getDueDate() + " " + timeStr;

        if (!item.isCountdown()) {
            return fullStr;
        }

        try {
            LocalDateTime dueDateTime = LocalDateTime.parse(fullStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, dueDateTime);

            if (duration.isNegative()) {
                long minutesPast = Math.abs(duration.toMinutes());
                if (minutesPast < 60) {
                    return "Expired (" + minutesPast + "m ago)";
                }
                long hoursPast = Math.abs(duration.toHours());
                return "Expired (" + hoursPast + "h ago)";
            } else {
                long days = duration.toDays();
                long hours = duration.toHours() % 24;
                long minutes = duration.toMinutes() % 60;
                if (days > 0) {
                    return String.format("%dd %02dh %02dm left", days, hours, minutes);
                } else if (hours > 0) {
                    return String.format("%02dh %02dm left", hours, minutes);
                } else {
                    return String.format("%02dm left", minutes);
                }
            }
        } catch (Exception e) {
            return fullStr;
        }
    }
}
