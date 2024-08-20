package dev.staniszak.app.model;

import java.io.File;

import dev.staniszak.app.utils.Utils;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MediaPlayerModel {

    private MediaPlayer player;
    private Duration duration;
    private boolean repeat = false;

    public MediaPlayerModel(String filepath) {
        File lastPlayed = new File(filepath);

        /* If we cannot find the last track that played before closing application. 
           (e.g. first execution of an app or user has deleted the track)
           Setup default audio with duration of 0.  
        */
        if (!lastPlayed.exists()) {
            lastPlayed = Utils.universalFileGen("/no-tracks/default.mp3", ".mp3");
            this.duration = new Duration(0.0);     
        }

        this.player = new MediaPlayer(new Media(lastPlayed.toURI().toString()));
        this.setRepeatCycles();
    }

    ////////Getters////////

    public MediaPlayer GetPlayer() {
        return this.player;
    } 

    public Duration getDuration() {
        return this.duration;
    }

    public boolean Repeat() {
        return repeat;
    }

    public void setRepeat(boolean isrepeat) {
        this.repeat = isrepeat;
        this.setRepeatCycles();
    }
    
    ////////Helpers Player////////

    public void seek(Duration duration) {
       player.seek(duration);
    }

    public void updateDuration() {
        this.duration = player.getMedia().getDuration();
        // Duration can be null if media has no duration (eg. resources\no-tracks\default.mp3).  
        if (this.duration == null) {
            this.duration = new Duration(0.0);
        }
    }
     
    public void setRepeatCycles() {
        player.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
    }

    public void changeMedia(String filepath) {

        Media selected_media = new Media( new File(filepath).toURI().toString());

        Double volume = this.player.getVolume();
        this.player.stop();
        this.player.dispose();

        this.player = new MediaPlayer(selected_media);
        this.setRepeatCycles();
        this.player.setVolume(volume); // <- keep volume settings.
        this.player.play();
    }

}
