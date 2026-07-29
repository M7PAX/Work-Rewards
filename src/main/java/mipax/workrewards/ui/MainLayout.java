package mipax.workrewards.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import mipax.workrewards.ui.views.AddWorkView;
import mipax.workrewards.ui.views.CompletedTasksView;
import mipax.workrewards.ui.views.WorkListView;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
    }

    private void createHeader() {
        H1 logo = new H1("Work Rewards");
        logo.addClassNames("text-l", "m-m");

        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.TASKS, "Work List", WorkListView.class),
                createTab(VaadinIcon.CHECK_CIRCLE, "Completed Tasks", CompletedTasksView.class),
                createTab(VaadinIcon.PLUS_CIRCLE, "Add Work", AddWorkView.class)
        );

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, tabs);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private Tab createTab(VaadinIcon icon, String title, Class<? extends com.vaadin.flow.component.Component> viewClass) {
        RouterLink link = new RouterLink(title, viewClass);
        link.addComponentAsFirst(icon.create());
        return new Tab(link);
    }
}
