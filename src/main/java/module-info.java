module dev.staniszak.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.media;
    requires transitive javafx.graphics;
    requires lombok;
    requires transitive com.fasterxml.jackson.databind;
    requires java.desktop;

    opens dev.staniszak.app to javafx.fxml;
    exports dev.staniszak.app;
    exports dev.staniszak.app.common;
}
