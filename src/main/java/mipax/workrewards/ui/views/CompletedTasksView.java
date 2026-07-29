package mipax.workrewards.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import mipax.workrewards.model.WorkItem;
import mipax.workrewards.service.WorkService;
import mipax.workrewards.ui.MainLayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@PageTitle("Completed Tasks | Work Rewards")
@Route(value = "completed", layout = MainLayout.class)
public class CompletedTasksView extends VerticalLayout {

    private final WorkService workService;

    private final VerticalLayout cardContainer = new VerticalLayout();
    private final TextField searchField = new TextField();
    private final Consumer<String> categoryListener = cat -> updateGridItems();

    public CompletedTasksView(WorkService workService) {
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
        H2 header = new H2("Completed Tasks Overview");
        header.getStyle().set("margin-top", "0");
        add(header);
    }

    private void createFilterToolbar() {
        searchField.setPlaceholder("Search completed work title or description...");
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

        Checkbox doneBox = new Checkbox(true);
        doneBox.getStyle().set("width", "36px").set("flex-shrink", "0");
        doneBox.addValueChangeListener(e -> {
            if (!e.getValue()) {
                undoTask(item);
            }
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
                .set("text-decoration", "line-through")
                .set("opacity", "0.7")
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

        Span completedBadge = new Span("Completed: " + (item.getCompletedAt() != null ? item.getCompletedAt() : ""));
        completedBadge.getStyle()
                .set("padding", "6px 12px")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-success-color-10pct)")
                .set("color", "var(--lumo-success-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500")
                .set("width", "160px")
                .set("min-width", "160px")
                .set("text-align", "center")
                .set("margin-right", "12px")
                .set("flex-shrink", "0");

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

        Button detailsBtn = new Button(VaadinIcon.INFO_CIRCLE.create(), e -> showDetailsDialog(item));
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button undoBtn = new Button("Undo", VaadinIcon.ARROW_LEFT.create(), e -> undoTask(item));
        undoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SUCCESS);

        Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> deleteTask(item));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        actions.add(detailsBtn, undoBtn, deleteBtn);

        card.add(doneBox, catBadge, textContainer, completedBadge, actions);
        card.setFlexGrow(1, textContainer);

        return card;
    }

    private void undoTask(WorkItem item) {
        item.setCompleted(false);
        workService.updateItem(item);
        Notification.show("Moved '" + item.getTitle() + "' back to Work List");
        updateView();
    }

    private void deleteTask(WorkItem item) {
        workService.deleteWork(item.getId());
        Notification.show("Permanently deleted task");
        updateView();
    }

    private void showDetailsDialog(WorkItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Completed Details");

        VerticalLayout content = new VerticalLayout();
        content.add(new Span("Title: " + item.getTitle()));
        content.add(new Span("Category: " + item.getCategory()));
        content.add(new Span("Completed At: " + (item.getCompletedAt() != null ? item.getCompletedAt() : "")));
        content.add(new Span("Due: " + WorkListView.formatDueDateOrCountdown(item)));
        content.add(new Span("Description: " + item.getDescription()));
        if (item.getHyperlink() != null && !item.getHyperlink().isEmpty()) {
            content.add(new Anchor(item.getHyperlink(), "Open Hyperlink: " + item.getHyperlink()));
        }

        Button undoBtn = new Button("Undo Task", VaadinIcon.ARROW_LEFT.create(), e -> {
            undoTask(item);
            dialog.close();
        });
        undoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button deleteBtn = new Button("Delete Task", VaadinIcon.TRASH.create(), e -> {
            deleteTask(item);
            dialog.close();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button close = new Button("Close", e -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(undoBtn, deleteBtn, close);
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
                .filter(WorkItem::isCompleted)
                .filter(item -> {
                    boolean matchesText = filterText.isEmpty()
                            || (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterText))
                            || (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterText));
                    boolean matchesCat = selectedCat == null || "All".equalsIgnoreCase(selectedCat)
                            || selectedCat.equalsIgnoreCase(item.getCategory());
                    return matchesText && matchesCat;
                })
                .sorted((a, b) -> {
                    String timeA = a.getCompletedAt() != null ? a.getCompletedAt() : "";
                    String timeB = b.getCompletedAt() != null ? b.getCompletedAt() : "";
                    return timeB.compareTo(timeA);
                })
                .collect(Collectors.toList());

        for (WorkItem item : filtered) {
            cardContainer.add(createTaskCard(item));
        }
    }
}
