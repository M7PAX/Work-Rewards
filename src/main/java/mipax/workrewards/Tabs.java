package mipax.workrewards;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.event.Event;
import java.io.IOException;
import java.util.Objects;

public class Tabs extends StackPane {
    @FXML
    private AnchorPane displayWork;
    @FXML
    private AnchorPane displayAdd;

    public void addTab(Event event) throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("add.fxml")));
        displayAdd.getChildren().setAll(node);
    }

    public void openTab(Event event) throws IOException {
        Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("list.fxml")));
        displayWork.getChildren().setAll(node);
    }
}
