module se233.adpro2 {
    requires javafx.controls;
    requires javafx.fxml;

    opens se233.adpro2 to javafx.fxml;
    exports se233.adpro2;
}
