module mipax.workrewards {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires org.kordamp.bootstrapfx.core;
    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.attach.util;
    requires jdk.jfr;

    opens mipax.workrewards to javafx.fxml;
    exports mipax.workrewards;
}