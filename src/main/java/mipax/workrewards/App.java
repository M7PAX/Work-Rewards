package mipax.workrewards;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Work Work = new Work();
        Work.getdb();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("tabs.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        stage.setTitle("Work Rewards");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
    }
}
