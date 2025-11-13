module mipax.workrewards.workrewards {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens mipax.workrewards to javafx.fxml;
    exports mipax.workrewards;
}