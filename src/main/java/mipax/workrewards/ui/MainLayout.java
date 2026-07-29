package mipax.workrewards.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import mipax.workrewards.service.WorkService;
import mipax.workrewards.ui.views.AddWorkView;
import mipax.workrewards.ui.views.CompletedTasksView;
import mipax.workrewards.ui.views.WorkListView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainLayout extends AppLayout {

    private static String selectedCategory = "All";
    private static final List<Consumer<String>> categoryListeners = new ArrayList<>();

    private final WorkService workService;
    private Tabs headerTabs;

    public MainLayout(WorkService workService) {
        this.workService = workService;
        setPrimarySection(Section.NAVBAR);
        setDrawerOpened(false);
        createHeader();
        createDrawer();
        setupHoverDrawer();
    }

    public static String getSelectedCategory() {
        return selectedCategory;
    }

    public static void addCategoryChangeListener(Consumer<String> listener) {
        categoryListeners.add(listener);
    }

    public static void removeCategoryChangeListener(Consumer<String> listener) {
        categoryListeners.remove(listener);
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        H1 logo = new H1("Work Rewards");
        logo.addClassNames("text-l", "m-m");

        headerTabs = new Tabs();
        buildHeaderTabs();

        Button addCategoryBtn = new Button(VaadinIcon.PLUS.create(), e -> openAddCategoryDialog());
        addCategoryBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        addCategoryBtn.setTooltipText("Add New Category");

        Button themeToggleBtn = new Button(VaadinIcon.MOON.create());
        themeToggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggleBtn.setAriaLabel("Toggle dark/light theme");
        themeToggleBtn.getStyle().set("margin-left", "auto");

        themeToggleBtn.addClickListener(e -> {
            getElement().executeJs(
                "const isDark = document.documentElement.getAttribute('theme') === 'dark';" +
                "if (isDark) {" +
                "  document.documentElement.removeAttribute('theme');" +
                "  localStorage.setItem('app-theme', 'light');" +
                "} else {" +
                "  document.documentElement.setAttribute('theme', 'dark');" +
                "  localStorage.setItem('app-theme', 'dark');" +
                "}" +
                "return !isDark;"
            ).then(Boolean.class, isDarkNow -> {
                if (Boolean.TRUE.equals(isDarkNow)) {
                    themeToggleBtn.setIcon(VaadinIcon.SUN_O.create());
                } else {
                    themeToggleBtn.setIcon(VaadinIcon.MOON.create());
                }
            });
        });

        getElement().executeJs(
            "const saved = localStorage.getItem('app-theme');" +
            "if (saved === 'dark') {" +
            "  document.documentElement.setAttribute('theme', 'dark');" +
            "}" +
            "return document.documentElement.getAttribute('theme') === 'dark';"
        ).then(Boolean.class, isDark -> {
            if (Boolean.TRUE.equals(isDark)) {
                themeToggleBtn.setIcon(VaadinIcon.SUN_O.create());
            } else {
                themeToggleBtn.setIcon(VaadinIcon.MOON.create());
            }
        });

        HorizontalLayout header = new HorizontalLayout(toggle, logo, headerTabs, addCategoryBtn, themeToggleBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void buildHeaderTabs() {
        headerTabs.removeAll();

        Tab addWorkTab = createTab(VaadinIcon.PLUS_CIRCLE, "Add Work", AddWorkView.class);
        headerTabs.add(addWorkTab);

        Tab allTab = new Tab("All");
        headerTabs.add(allTab);

        List<String> categories = workService != null ? workService.getCategories() : List.of();
        for (String cat : categories) {
            if (!"Main".equalsIgnoreCase(cat)) {
                Tab catTab = new Tab(cat);
                headerTabs.add(catTab);
            }
        }

        headerTabs.addSelectedChangeListener(e -> {
            Tab selected = e.getSelectedTab();
            if (selected != null) {
                String label = selected.getLabel();
                if (!"Add Work".equalsIgnoreCase(label) && label != null && !label.isEmpty()) {
                    selectedCategory = label;
                    for (Consumer<String> l : new ArrayList<>(categoryListeners)) {
                        l.accept(selectedCategory);
                    }
                }
            }
        });
    }

    private void openAddCategoryDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New Category");

        TextField catNameInput = new TextField("Category Name");
        catNameInput.setPlaceholder("e.g. Finance, Fitness...");
        catNameInput.setWidthFull();

        Button saveBtn = new Button("Create", e -> {
            String val = catNameInput.getValue();
            if (val != null && !val.trim().isEmpty()) {
                workService.addCustomCategory(val.trim());
                buildHeaderTabs();
                Notification.show("Category '" + val.trim() + "' created!");
                dialog.close();
            } else {
                Notification.show("Please enter a category name");
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        VerticalLayout layout = new VerticalLayout(catNameInput);
        dialog.add(layout);
        dialog.getFooter().add(saveBtn, cancelBtn);
        dialog.open();
    }

    private void createDrawer() {
        Tabs drawerTabs = new Tabs();
        drawerTabs.setOrientation(Tabs.Orientation.VERTICAL);
        drawerTabs.add(
                createTab(VaadinIcon.TASKS, "Work List", WorkListView.class),
                createTab(VaadinIcon.CHECK_CIRCLE, "Completed Tasks", CompletedTasksView.class),
                createTab(VaadinIcon.FOLDER, "Projects", null),
                createTab(VaadinIcon.COG, "Settings", null)
        );

        addToDrawer(drawerTabs);
    }

    private void setupHoverDrawer() {
        getElement().executeJs(
            "const appLayout = this;" +
            "let pinnedOpen = false;" +

            "const headStyle = document.createElement('style');" +
            "headStyle.textContent = `" +
            "  html[theme~=\"dark\"], [theme~=\"dark\"] {" +
            "    --lumo-base-color: #000000 !important;" +
            "    --lumo-body-text-color: rgba(255, 255, 255, 0.95) !important;" +
            "    --lumo-header-text-color: #ffffff !important;" +
            "    --lumo-tint-5pct: rgba(255, 255, 255, 0.05) !important;" +
            "    --lumo-contrast-5pct: rgba(255, 255, 255, 0.08) !important;" +
            "    --lumo-contrast-10pct: rgba(255, 255, 255, 0.14) !important;" +
            "    background-color: #000000 !important;" +
            "    color: #ffffff !important;" +
            "  }" +
            "  vaadin-app-layout[theme~=\"dark\"]," +
            "  vaadin-app-layout[theme~=\"dark\"]::part(drawer)," +
            "  vaadin-app-layout[theme~=\"dark\"]::part(navbar) {" +
            "    background-color: #000000 !important;" +
            "    color: #ffffff !important;" +
            "    border-color: #222222 !important;" +
            "  }" +
            "  vaadin-app-layout::part(drawer) {" +
            "    top: var(--vaadin-app-layout-navbar-offset-top, 44px) !important;" +
            "    height: calc(100vh - var(--vaadin-app-layout-navbar-offset-top, 44px)) !important;" +
            "    z-index: 10 !important;" +
            "  }" +
            "  vaadin-app-layout:not([drawer-opened])::part(drawer) {" +
            "    visibility: visible !important;" +
            "    transform: translateX(0) !important;" +
            "    width: 64px !important;" +
            "    overflow: hidden !important;" +
            "    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);" +
            "    transition: width 0.2s ease-in-out;" +
            "  }" +
            "  vaadin-app-layout[drawer-opened]::part(drawer) {" +
            "    visibility: visible !important;" +
            "    width: 240px !important;" +
            "    transition: width 0.2s ease-in-out;" +
            "  }" +
            "  vaadin-app-layout::part(content) {" +
            "    padding-left: 64px !important;" +
            "  }" +
            "  vaadin-app-layout > :not([slot]) {" +
            "    margin-left: 64px !important;" +
            "    width: calc(100% - 64px) !important;" +
            "    box-sizing: border-box !important;" +
            "  }" +
            "  .tab-link {" +
            "    display: inline-flex;" +
            "    align-items: center;" +
            "    gap: 16px;" +
            "    width: 100%;" +
            "    text-decoration: none;" +
            "    color: inherit;" +
            "    cursor: pointer;" +
            "    border-radius: 12px !important;" +
            "  }" +
            "  .tab-link vaadin-icon {" +
            "    min-width: 24px;" +
            "    width: 24px;" +
            "    height: 24px;" +
            "    flex-shrink: 0;" +
            "  }" +
            "  .tab-text {" +
            "    white-space: nowrap;" +
            "    overflow: hidden;" +
            "    transition: opacity 0.2s ease-in-out;" +
            "  }" +
            "  vaadin-app-layout:not([drawer-opened]) [slot=\"drawer\"] .tab-text {" +
            "    opacity: 0;" +
            "    pointer-events: none;" +
            "  }" +
            "  vaadin-app-layout[drawer-opened] [slot=\"drawer\"] .tab-text {" +
            "    opacity: 1;" +
            "  }" +
            "  .task-card {" +
            "    background: var(--lumo-base-color);" +
            "    border-radius: 16px;" +
            "    padding: 16px 20px;" +
            "    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);" +
            "    border: 1px solid var(--lumo-contrast-10pct);" +
            "    transition: transform 0.15s ease, box-shadow 0.15s ease;" +
            "    margin-bottom: 12px;" +
            "  }" +
            "  .task-card:hover {" +
            "    transform: translateY(-2px);" +
            "    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);" +
            "  }" +
            "  html[theme~=\"dark\"] .task-card {" +
            "    background: #121212 !important;" +
            "    border: 1px solid #242424 !important;" +
            "    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.6) !important;" +
            "  }" +
            "`;" +
            "document.head.appendChild(headStyle);" +

            "const injectShadowStyle = () => {" +
            "  if (appLayout.shadowRoot) {" +
            "    const s = document.createElement('style');" +
            "    s.textContent = `" +
            "      :host(:not([drawer-opened])) [part=\"drawer\"] {" +
            "        visibility: visible !important;" +
            "        transform: translateX(0) !important;" +
            "        width: 64px !important;" +
            "        overflow: hidden !important;" +
            "      }" +
            "      :host([drawer-opened]) [part=\"drawer\"] {" +
            "        visibility: visible !important;" +
            "        width: 240px !important;" +
            "      }" +
            "      :host [part=\"content\"] {" +
            "        padding-left: 64px !important;" +
            "      }" +
            "    `;" +
            "    appLayout.shadowRoot.appendChild(s);" +
            "  }" +
            "};" +
            "injectShadowStyle();" +

            "const initHoverAndClick = () => {" +
            "  const drawerPart = appLayout.shadowRoot && appLayout.shadowRoot.querySelector('[part=\"drawer\"]');" +
            "  const drawerSlot = appLayout.querySelector('[slot=\"drawer\"]');" +
            "  const targets = [drawerPart, drawerSlot].filter(Boolean);" +
            "  if (targets.length > 0) {" +
            "    targets.forEach(t => {" +
            "      t.addEventListener('mouseenter', () => { appLayout.drawerOpened = true; });" +
            "      t.addEventListener('mouseleave', () => { appLayout.drawerOpened = false; });" +
            "      t.addEventListener('click', () => {" +
            "        setTimeout(() => { appLayout.drawerOpened = false; }, 100);" +
            "      });" +
            "    });" +
            "  } else {" +
            "    setTimeout(initHoverAndClick, 50);" +
            "  }" +
            "};" +
            "initHoverAndClick();"
        );
    }

    private Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
        Span text = new Span(title);
        text.addClassName("tab-text");

        if (viewClass != null) {
            RouterLink link = new RouterLink(viewClass);
            link.add(icon.create(), text);
            link.addClassName("tab-link");
            return new Tab(link);
        } else {
            Span container = new Span(icon.create(), text);
            container.addClassName("tab-link");
            return new Tab(container);
        }
    }
}
