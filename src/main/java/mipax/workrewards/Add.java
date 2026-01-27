package mipax.workrewards;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class Add {
    @FXML
    private ToggleButton HLCB, DDCB;
    @FXML
    private TextField HLInput, dueH, dueMin, newCategoryInput, workInput;
    @FXML
    private Button addWorkB;
    @FXML
    private ChoiceBox<?> categoryC;
    @FXML
    private TextArea descriptionInput;
    @FXML
    private DatePicker dueDate;
    @FXML
    private HBox dueDateBox;

    ArrayList<String[]> db = new ArrayList<>();

    boolean toggleDD = true;
    boolean toggleHL = true;

    @FXML
    void addWork(MouseEvent event) {
        Work Work = new Work(db);

        LocalDate dueDate = this.dueDate.getValue();
        String dueTime = dueH.getText() + ":" + dueMin.getText();

        String work = IDMaker(categoryC.toString()) + ";"
            + workInput.getText() + ";"
            + descriptionInput.getText() + ";"
            + categoryC.toString() + ";"
            + dueDate.toString() + ";"
            + dueTime + ";"
            + HLInput.getText();

        Work.add(work);
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

    String IDMaker(String category) {
        int count = 0;

        for (int i = 0; i < db.size(); i++) {
            String[] row = db.get(i);

            if (Objects.equals(row[3], category)) {
                count += 1;
            }
        }
        count++;

        return category + "-" + String.valueOf(count);
    }

}
