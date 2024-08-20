package dev.staniszak.app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class MediaPlayerView {

    private BorderPane root;
    private Canvas canvas;
    private Pane mediaPane;
    private MenuBar menuBar;
    private TreeView<String> fileView;
    private Button playButton;
    private Button repeatButton;
    private Slider durationSlider;
    private Label playTime; 
    private Slider volumeSlider;


    public MediaPlayerView() {

        ////////Buttons////////

        // Button repsonsible for play and stop track 
        playButton = new Button("Play");
        playButton.setMinWidth(50);
        playButton.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 5px;");

        // Repeat track button
        this.repeatButton = new Button("Repeat Song");
        repeatButton.setMinWidth(60);
        repeatButton.setStyle("-fx-padding: 5px 10px; -fx-background-radius: 5px;");
        HBox.setMargin(repeatButton, new Insets(5, 50, 0, 0));

        ////////Labels////////

        // Duration label
        Label durationLabel = new Label("Time: ");
        durationLabel.setMinWidth(40);

        // Play label
        playTime = new Label("0");
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);

        // Volume label
        Label volumeLabel = new Label("Vol: ");
        volumeLabel.setMinWidth(30);

        ////////Sliders////////

        this.durationSlider =  new Slider();
        HBox.setHgrow(durationSlider,Priority.ALWAYS);
        /* By default the value of the slider is in range [0.0-100.0].
           MediaPlayer accepts some values (such as volume) in range [0.0-1.0].
           We setMax() slider to match MediaPlayer.
           Also we can avoid using division in some operations if we use [0.0-1.0] range.  
         */
        durationSlider.setMax(1.0);  
        durationSlider.setMinWidth(50);
        durationSlider.setMaxWidth(Double.MAX_VALUE);
        this.durationSlider.getStyleClass().add("duration-slider");

        // Volume slider
        volumeSlider = new Slider();
        volumeSlider.setMax(1.0);      
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMinWidth(30);


        ////////Regions////////
        Region spacer = new Region();
        spacer.setMinWidth(5);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region play_spacer = new Region();
        play_spacer.setPrefWidth(20);
        play_spacer.setMinWidth(5);

        ////////Combine////////
        HBox mediaControl =  new HBox();
        mediaControl.getChildren().addAll(
                               playButton, play_spacer, 
                               volumeLabel, volumeSlider, spacer, repeatButton);

        mediaControl.setPadding(new Insets(5, 0, 0, 0));                       
                       

        HBox mediaDuration = new HBox();
        mediaDuration.getChildren().addAll(durationLabel, 
                                           durationSlider, 
                                           playTime); 


        VBox mediaBar =  new VBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        mediaBar.getChildren().addAll(mediaDuration, mediaControl);

        ////Media Pane with Canvas////
        this.canvas = new Canvas(); 
        this.mediaPane = new Pane(this.canvas);
        this.canvas.widthProperty().bind(mediaPane.widthProperty());
        this.canvas.heightProperty().bind(mediaPane.heightProperty());
        this.mediaPane.getStyleClass().add("media-pane");
        
        ////Menu Bar////
        this.menuBar = new MenuBar();

        Menu organiseMenu = new Menu("Organise");

        MenuItem changeDirectoryItem = new MenuItem("Change Default Directory");

        organiseMenu.getItems().addAll(changeDirectoryItem);

        menuBar.getMenus().add(organiseMenu);

        ////File viewer////
        fileView =  new TreeView<>();

        ////Root node////
        root = new BorderPane();
        root.setPadding(new Insets(10)); // -fx-padding: 10px;
        fileView.prefWidthProperty().bind(root.widthProperty().multiply(0.2)); // adjast file selector size;
        root.setTop(menuBar);
        root.setCenter(this.mediaPane);
        root.setLeft(fileView);
        root.setBottom(mediaBar);
        BorderPane.setAlignment(mediaPane, Pos.CENTER);
    }
    ////Lombock Getters
}
