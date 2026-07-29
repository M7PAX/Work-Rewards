package mipax.workrewards;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class Add implements Initializable {
    Work Work = new Work();

    @FXML
    private ToggleButton HLCB, DDCB;
    @FXML
    private TextField HLInput, dueH, dueMin, newCategoryInput, workInput;
    @FXML
    private Button addWorkB;
    @FXML
    private ChoiceBox<String> categoryC;
    @FXML
    private TextArea descriptionInput;
    @FXML
    private DatePicker dueDate;
    @FXML
    private HBox dueDateBox;

    boolean toggleDD = true;
    boolean toggleHL = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cat();
//        addWorkB.setDisable(Objects.equals(workInput.getText(), ""));

    }


    @FXML
    void cat() {
        ObservableList<String> ol = FXCollections.observableList(Work.getCategories());
        categoryC.setItems(ol);
    }

    @FXML
    void addWork(MouseEvent event) throws IOException {
        LocalDate dueDate = this.dueDate.getValue();
        String dueHour = "00";
        String dueMinute = "00";
        String DD = "00-00-00";
        if (!Objects.equals(dueH.getText(), "")) {
            dueHour = dueH.getText();
        }
        if (!Objects.equals(dueMin.getText(), "")) {
            dueMinute = dueMin.getText();
        }
        if (!Objects.equals(dueDate, null)) {
            DD = dueDate.toString();
        }
        String dueTime = dueHour + ":" + dueMinute;

        String cate = "Main";
        if (!Objects.equals(categoryC.getValue(), null)) {
            cate = categoryC.getValue();
        }

        String work = IDMaker(cate) + ";"
            + workInput.getText() + ";"
            + descriptionInput.getText() + ";"
            + cate + ";"
            + DD + ";"
            + dueTime + ";"
            + HLInput.getText();

        System.out.println(work);
        Work.add(work);
        Work.save();
    }

    String IDMaker(String category) {
        int count = 0;
        System.out.println(Arrays.deepToString(Work.db.toArray()));


        for (int i = 0; i < Work.db.size(); i++) {
            String[] row = Work.db.get(i);

            if (Objects.equals(row[3], category)) {
                count += 1;
            }
        }
        count++;

        return category + "-" + String.valueOf(count);
    }

    @FXML
    void toggleDueDate(MouseEvent event) {
        if (toggleDD) {
            toggleDD = false;
            dueDateBox.setDisable(toggleDD);
        }
        else {
            toggleDD = true;
            dueDateBox.setDisable(toggleDD);
        }
    }

    @FXML
    void toggleHL(MouseEvent event) {
        if (toggleHL) {
            toggleHL = false;
            HLInput.setDisable(toggleHL);
        }
        else {
            toggleHL = true;
            HLInput.setDisable(toggleHL);
        }
    }
}
