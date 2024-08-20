package dev.staniszak.app.controller;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import dev.staniszak.app.common.UserConfig;
import dev.staniszak.app.model.MediaPlayerModel;
import dev.staniszak.app.utils.JsonConfigManager;
import dev.staniszak.app.utils.Utils;
import dev.staniszak.app.view.MediaPlayerView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
 * Controller.
 * Connects application view (GUI) to the application model (Audio Player).
 * View layer of this application is mostly static, so controller supports most of GUI Interactivity. 
 * 
 */


public class MediaPlayerController {

    private MediaPlayerView view;
    private MediaPlayerModel model;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Stage primaryStage;
    private DirectoryChooser directoryChooser;
    private UserConfig userConfig;
    private String filePath;
    private String lastPlayed;
    private Dimension dimension;
    private double changeWidth;

    public MediaPlayerController(MediaPlayerView view, Stage primaryStage) {
        this.view = view;
        try {
        this.userConfig = JsonConfigManager.loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.filePath = userConfig.getUser_star_directory();
        this.lastPlayed = userConfig.getUser_lastplayed_track();

        this.model = new MediaPlayerModel(this.lastPlayed);
        this.primaryStage = primaryStage;

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        directoryChooser.setInitialDirectory( new File(this.filePath));

        this.dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.changeWidth = dimension.getWidth() / 2.2;

        init();

    }

    private void init() {

        ////////Buttons////////

        this.view.getPlayButton().setOnAction( (event) -> {

            Status status = model.GetPlayer().getStatus();

            if ( status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) {

                if (atEndOfMedia) {
                  model.seek(model.GetPlayer().getStartTime()); 
                  atEndOfMedia = false;
                }
                model.GetPlayer().play();

            } else if (status == Status.PLAYING || status == Status.STALLED) {
                model.GetPlayer().pause();
            }  
        });

        view.getRepeatButton().setOnAction( (event) ->
        
        {
            if(view.getRepeatButton().getStyleClass().contains("repeat-button-clicked")) {
                view.getRepeatButton().getStyleClass().remove("repeat-button-clicked");
                model.setRepeat(false);
            } else {
                view.getRepeatButton().getStyleClass().add("repeat-button-clicked");
                model.setRepeat(true);
            }

        });

        ////////Menues////////

        /*
         How it looks for the user: 
         User clicks on Menu Item 'Change Default Directory'
         Native file explorer UI pops up, user selects directory.
         Selected directory is marked as root and File navigation menu is populated with the content of the directory.
         
         This event handler is Responsible for:
         Opens menu dialog for user to choose directory.
         Checks if the selected File is a directory. 
         Calls getNodesForDirectory to populate File View with directories and files.
         */

        this.view.getMenuBar().getMenus().get(0).setOnAction((event) -> {

            File selectedDirectoy = directoryChooser.showDialog(this.primaryStage);

            if (selectedDirectoy != null && selectedDirectoy.isDirectory()) {
                this.filePath = selectedDirectoy.getAbsolutePath();
                view.getFileView().setRoot(Utils.getNodesForDirectory(selectedDirectoy, true));
            }
        });

        ////////File View////////

        /* Initialize File View with default or user-selected directory. */
        File directory =  new File(this.filePath);
        this.view.getFileView().setRoot(Utils.getNodesForDirectory(directory, true));

        /* Tree Item with the name of the Last Played track is marked as selected. */ 
        if (this.lastPlayed != null) {
        this.view.getFileView().getSelectionModel()
        .select(Utils.findTreeItemByValue(view.getFileView().getRoot(), Utils.getFileName(this.lastPlayed)));
        } 
        
        /* How it looks for the user: 
           User clicks on the track name in the File navigation menu on the left side of the screen,
           The track gets marked as selected.
           The new track starts playing.
            
           This event handler is Responsible for:
           When user double clicks on ANY place in the File navigation menu (File View).
           Event Hadler checks currently selected Tree Item.
           if the item passes all checks, method calls Utils.findStarNode to construct absolute file path to the selected file.
           Calls model to change track (which creates new MediaPlayer, not my implementation btw, as this is how JavaFx media Player works) 
           and we have to initialize newly created player.
         */
        this.view.getFileView().addEventHandler(MouseEvent.MOUSE_CLICKED, (event) ->
        {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = this.view.getFileView().getSelectionModel().getSelectedItem();
                    /* selectedItem.isLeaf() checks if the selected Item does not have any children. 
                       (If an item has no children it is either a file or an empty directory, so we also check for .mp3).
                    */ 
                if (selectedItem != null && selectedItem.isLeaf() && selectedItem.getValue().endsWith(".mp3")) {
                    // Construct absolute file path to the selected file.
                    this.lastPlayed = this.filePath + Utils.findStarNode(selectedItem.getParent(), selectedItem.getValue());
                    // Change track and Init new player.
                    model.changeMedia(this.lastPlayed);
                    this.initPlayer();
                } 
            }

        });

        ////////Sliders////////

        this.view.getDurationSlider().valueProperty().addListener((ov) -> { 
            if (view.getDurationSlider().isValueChanging()) {
               /* Seeks player to the new playback time
                  eg. model.seek(totalDuration * 0.5) move to the middle of the track.
                                                 0.5  is a value of the slider.              
               */
               model.seek(model.getDuration().multiply(view.getDurationSlider().getValue()));
            }
        });

        this.view.getVolumeSlider().valueProperty().addListener((ov) -> {
            
            if (view.getVolumeSlider().isValueChanging()) {
                /* Sets the audio playback volume. 
                   Player volume accepts values in [0.0, 1.0] range.
                   In the view, we have set up slider to match this range. 
                */
                model.GetPlayer().setVolume(view.getVolumeSlider().getValue());
            }
        });


        ////////Player////////

        this.initPlayer(); 


        ///////Stage////////

        /* If width of Audio Player window is around half of the user screen,
           Remove audio visualizer and set File navigation Menu to take up most of the screen.
           */
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
    
            if (width < this.changeWidth && view.getRoot().getLeft() != null) {
                view.getRoot().setLeft(null);
                view.getRoot().setCenter(view.getFileView());
            } else if (width > this.changeWidth) {
                view.getRoot().setCenter(view.getMediaPane());
                view.getRoot().setLeft(view.getFileView());
            }
        });


        /*   
          When user closes application, save the location of the user directory and of the last played track.
         */
        this.primaryStage.setOnCloseRequest((event) -> {

            this.userConfig.setUser_star_directory(this.filePath); 
            this.userConfig.setUser_lastplayed_track(this.lastPlayed);

            try {
            JsonConfigManager.saveConfig(this.userConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    /* 
    Update View: Play Time label, Duration and Volume slider. 
    */
    private void updateView() {
        Duration currentTime = model.GetPlayer().getCurrentTime();
        view.getPlayTime().setText(Utils.formatTime(currentTime, model.getDuration()));
        view.getDurationSlider().setDisable(model.getDuration().isUnknown());
        if (!view.getDurationSlider().isDisabled() && model.getDuration().greaterThan(Duration.ZERO) && 
            !view.getDurationSlider().isValueChanging()) {   
             view.getDurationSlider().setValue(currentTime.toSeconds() / model.getDuration().toSeconds());
        }

        if (!view.getVolumeSlider().isValueChanging()) {
            view.getVolumeSlider().setValue(model.GetPlayer().getVolume());
           }
    }


    private void initPlayer() {

        model.GetPlayer().currentTimeProperty().addListener(ov -> updateView());

        model.GetPlayer().setOnPlaying(() -> {
            if (stopRequested) {
                model.GetPlayer().pause();
                stopRequested = false;
            } else {
                view.getPlayButton().setText("||");
            }
        });

        model.GetPlayer().setOnPaused(() -> {
            view.getPlayButton().setText("Play");
        });

        model.GetPlayer().setOnReady(() -> {
            model.updateDuration();
            updateView();
        });

        model.GetPlayer().setOnEndOfMedia(() -> {
            if (!model.Repeat() && !selectNextItem()) {
                
            } 
        });

        this.addAudioSpectrumListener();

        /* <- Reminder to use Animation timer for more complex audio visualization. 
              Simple visualization works fine without it.      
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Triggered every frame
            }
        };
        timer.start();
        */
    }
        

    /* Listens for the magnitudes.
       Gets an array of magnitude values every 16ms and draws them on canvas as rectangles. 
       */       
    private void addAudioSpectrumListener() {
        model.GetPlayer().setAudioSpectrumInterval(0.016); // Update every 16ms (~60fps)
        model.GetPlayer().setAudioSpectrumNumBands(128); // Number of frequency bands
        model.GetPlayer().setAudioSpectrumThreshold(-90);     
        model.GetPlayer().setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
            this.drawWaveform(magnitudes); 
        });
    }

    private void drawWaveform(float[] magnitudes) {
        GraphicsContext gc = view.getCanvas().getGraphicsContext2D();
        gc.clearRect(0, 0, view.getCanvas().getWidth(), view.getCanvas().getHeight());
        gc.setEffect(new GaussianBlur(5));
        //gc.setLineCap(StrokeLineCap.ROUND);

        double width = view.getCanvas().getWidth();
        double height = view.getCanvas().getHeight();
        double barWidth = width / magnitudes.length;
        double halfHeight = height / 2;

        for (int i = 0; i < magnitudes.length; i++) {
            double magnitude = magnitudes[i] + 90; // Adjust magnitude to be positive // alternative  - mediaPlayer.getSpektrumThreshold();
            double barHeight = magnitude * 3; // Scale bar height

            // Color of rect is based on the location in the array.
            Color color = Color.hsb(i * -180.0 / magnitudes.length, 1.0, 1.0); 
            gc.setFill(color);

            gc.fillRect(i * barWidth, halfHeight - barHeight / 2, barWidth, barHeight);

            // Looks better with shadows, I think.
            DropShadow dropShadow = new DropShadow();
            dropShadow.setOffsetY(2.0);
            dropShadow.setOffsetX(2.0);
            dropShadow.setColor(Color.rgb(50, 50, 50, 0.5));
            gc.setEffect(dropShadow);
        }
        
        /*  
        Effects stay drawn even after clearing canvas.
        It looks interesting, but it is not what we want,            
        so we need to remove them before drawing next magnitudes.
        */
        gc.setEffect(null);  
    }
    
    /*
    If we are not playing song on repeat this method gets called.
    It searches File navigation menu (File View) for the next track to play.
     */

    private boolean selectNextItem() {
        // Get next sibling of the currently selected track.
        TreeItem<String> siblingItem = view.getFileView().getSelectionModel().getSelectedItem().nextSibling();
        
        /* Checks if the sibling item exists, if not, recursively searches for the next item in the tree. */
        siblingItem = checkSiblingItem(siblingItem);

        // If next item is a directory, get first track inside directory.  
        while (!siblingItem.isLeaf()) {
               siblingItem = siblingItem.getChildren().get(0);
        }
        // Show user currently selected track and play it.
        this.view.getFileView().getSelectionModel().select(siblingItem);
        this.lastPlayed = this.filePath + Utils.findStarNode(siblingItem.getParent(), siblingItem.getValue());
        model.changeMedia(this.lastPlayed);
        initPlayer();

        return true;
    } 

    private TreeItem<String> checkSiblingItem(TreeItem<String> siblingItem) {
        // if next sibling does not exist (eg. end the end of library or tracklist).  
        if (siblingItem == null) {
            /*  
              Check if the parent directory of the selected item is root directory (see. Note-root-design). 
              If parent directory is root, then we select first item in the library (basically, we start playing from the beginning).
            */
            if (view.getFileView().getSelectionModel().getSelectedItem().getParent().getValue().contains(" \u2605")) {
                siblingItem = view.getFileView().getSelectionModel().getSelectedItem().getParent().getChildren().get(0);
            } else {
                /* Otherwise, we are recursively going up the tree 
                until we find next sibling that is not null or reach root directory.
                Either way, we are getting a valid Tree Item.*/
                this.view.getFileView().getSelectionModel().select(view.getFileView().getSelectionModel().getSelectedItem().getParent());   
                siblingItem = checkSiblingItem(view.getFileView().getSelectionModel().getSelectedItem().nextSibling());
            }
        }
        return siblingItem;
    }

    // Note-root-design: (by design of application root directory Always marked by a star - " \u2605", even if user is using custome directory).
}
