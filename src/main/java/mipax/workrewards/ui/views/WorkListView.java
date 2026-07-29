package mipax.workrewards.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import mipax.workrewards.model.WorkItem;
import mipax.workrewards.service.WorkService;
import mipax.workrewards.ui.MainLayout;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Work List | Work Rewards")
@Route(value = "", layout = MainLayout.class)
public class WorkListView extends VerticalLayout {

    private final WorkService workService;

    private final ProgressBar progressBar = new ProgressBar();
    private final Span progressLabel = new Span();
    private final Grid<WorkItem> grid = new Grid<>(WorkItem.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();

    public WorkListView(WorkService workService) {
        this.workService = workService;
        setSizeFull();
        setPadding(true);

        createProgressHeader();
        createFilterToolbar();
        createGrid();

        add(grid);
        updateView();
    }

    private void createProgressHeader() {
        H2 header = new H2("Work Rewards Overview");
        header.getStyle().set("margin-top", "0");

        progressBar.setWidthFull();
        HorizontalLayout progressBox = new HorizontalLayout(progressLabel, progressBar);
        progressBox.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        progressBox.setWidthFull();

        add(header, progressBox);
    }

    private void createFilterToolbar() {
        searchField.setPlaceholder("Search work title or description...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGridItems());

        categoryFilter.setPlaceholder("All Categories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setItems(workService.getCategories());
        categoryFilter.addValueChangeListener(e -> updateGridItems());

        Button refreshBtn = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> updateView());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, categoryFilter, refreshBtn);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();

        add(toolbar);
    }

    private void createGrid() {
        grid.setSizeFull();

        grid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox(item.isCompleted());
            checkbox.addValueChangeListener(e -> {
                item.setCompleted(e.getValue());
                workService.updateItem(item);
                if (e.getValue()) {
                    Notification.show("Moved to Completed Tasks: " + item.getTitle());
                }
                updateView();
            });
            return checkbox;
        }).setHeader("Done").setWidth("80px").setFlexGrow(0);

        grid.addColumn(WorkItem::getId).setHeader("ID").setSortable(true).setWidth("120px").setFlexGrow(0);
        grid.addColumn(WorkItem::getCategory).setHeader("Category").setSortable(true).setWidth("130px").setFlexGrow(0);
        grid.addColumn(WorkItem::getTitle).setHeader("Work Title").setSortable(true).setFlexGrow(1);
        grid.addColumn(WorkItem::getDescription).setHeader("Description").setFlexGrow(2);

        grid.addColumn(item -> {
            String date = item.getDueDate() != null ? item.getDueDate() : "";
            String time = item.getDueTime() != null ? item.getDueTime() : "";
            return (date + " " + time).trim();
        }).setHeader("Due Date & Time").setSortable(true).setWidth("180px").setFlexGrow(0);

        grid.addComponentColumn(item -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (item.getHyperlink() != null && !item.getHyperlink().trim().isEmpty()) {
                Anchor link = new Anchor(item.getHyperlink(), VaadinIcon.EXTERNAL_LINK.create());
                link.setTarget("_blank");
                actions.add(link);
            }

            Button detailsBtn = new Button(VaadinIcon.INFO_CIRCLE.create(), e -> showDetailsDialog(item));
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> {
                workService.deleteWork(item.getId());
                Notification.show("Deleted " + item.getId());
                updateView();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            actions.add(detailsBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("160px").setFlexGrow(0);
    }

    private void showDetailsDialog(WorkItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Work Details: " + item.getId());

        VerticalLayout content = new VerticalLayout();
        content.add(new Span("Title: " + item.getTitle()));
        content.add(new Span("Category: " + item.getCategory()));
        content.add(new Span("Due: " + item.getDueDate() + " " + item.getDueTime()));
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
        categoryFilter.setItems(workService.getCategories());
        updateGridItems();
        updateProgress();
    }

    private void updateProgress() {
        double progress = workService.getCompletionProgress();
        progressBar.setValue(progress);
        long total = workService.getAllItems().size();
        long done = workService.getAllItems().stream().filter(WorkItem::isCompleted).count();
        progressLabel.setText(String.format("Progress: %d / %d completed (%.0f%%)", done, total, progress * 100));
    }

    private void updateGridItems() {
        List<WorkItem> items = workService.getAllItems();
        String filterText = searchField.getValue() != null ? searchField.getValue().toLowerCase().trim() : "";
        String selectedCategory = categoryFilter.getValue();

        List<WorkItem> filtered = items.stream()
                .filter(item -> !item.isCompleted())
                .filter(item -> {
                    boolean matchesText = filterText.isEmpty()
                            || (item.getTitle() != null && item.getTitle().toLowerCase().contains(filterText))
                            || (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterText));
                    boolean matchesCat = selectedCategory == null || selectedCategory.isEmpty()
                            || selectedCategory.equalsIgnoreCase(item.getCategory());
                    return matchesText && matchesCat;
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }
}
