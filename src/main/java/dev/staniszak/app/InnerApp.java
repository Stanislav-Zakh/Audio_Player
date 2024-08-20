package dev.staniszak.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

import dev.staniszak.app.controller.MediaPlayerController;
import dev.staniszak.app.utils.JsonConfigManager;
import dev.staniszak.app.view.MediaPlayerView;

/**
 * JavaFX App
 */
public class InnerApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        /* Creat default config file and audio library if do not exist. */ 
        JsonConfigManager.writeDefaultConfig();
        JsonConfigManager.initDefaultDir(JsonConfigManager.getLIB_FILE_PATH());

        MediaPlayerView view = new MediaPlayerView();
        /* Controller provides functionality for the View and connects it with model */
        MediaPlayerController controller = new MediaPlayerController(view, stage);
        BorderPane root = view.getRoot();
        scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/mediaplayer.css").toExternalForm());
        stage.setTitle("MVC media player");
        stage.setScene(scene);
        stage.setMaximized(true); // <- Similar to setting stage to the full screen. 
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}