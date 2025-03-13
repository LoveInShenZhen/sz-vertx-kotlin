module io.kts.javafxdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens io.kts.javafxdemo to javafx.fxml;
    exports io.kts.javafxdemo;
}