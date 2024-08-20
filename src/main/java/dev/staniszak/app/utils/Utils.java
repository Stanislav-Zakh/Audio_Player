package dev.staniszak.app.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.scene.control.TreeItem;
import javafx.util.Duration;

/*
 * Class consist of methods that:
 * 1. Did not belong to the controller or model class.
 * 2. Have been used in several classes.
 * 3. Where too verbose and could be rearranged into the separate class without tedious refactoring process.
 */


public class Utils {

    /* Helper method that formats time into the appropriate format for the view */

    public static String formatTime(Duration played, Duration total) {
        // By default duration is in mills, so we turn them into minutes.
        int playedMinutes = (int)Math.floor(played.toMinutes());
        int totalMinutes = (int)Math.floor(total.toMinutes());

        int playedHours = (int)Math.floor(played.toHours());// <- get number of played hours
        int totalHours = (int)Math.floor(total.toHours());

        if (totalHours > 0) {
            totalMinutes -= totalHours * 60; // <- if track goes more than an hour, remove minutes from total time.
            if (playedHours > 0) {
                playedMinutes -= playedHours * 60;  
            }
        } 

        // Get seconds
        int playedSeconds = (int)Math.floor(played.toSeconds()) - playedMinutes * 60;
        int totalSeconds = (int)Math.floor(total.toSeconds()) - totalMinutes * 60;

        if (totalHours > 0) {
            return String.format("%d:%02d:%02d/%d:%02d:%02d", 
                playedHours, playedMinutes, playedSeconds,
                totalHours, totalMinutes, totalSeconds);
        } else {
            return String.format("%02d:%02d/%02d:%02d",
                playedMinutes, playedSeconds, totalMinutes, 
                totalSeconds);
        }
    }

    /*
    Builds a Tree of the TreeItem<String> from passed in directory.  
    When first called, marks root directory with a star - " \u2605".
    Marking root directory makes some search algorithms (eg. findStarNode()) easier to implement, also it looks nice.
    */     

     public static TreeItem<String> getNodesForDirectory(File directory, boolean isParent) {
        TreeItem<String> root = new TreeItem<String>(directory.getName());
        if (isParent) {
          root.setValue(directory.getName() + " \u2605");
        }
        for(File f : directory.listFiles()) {
            System.out.println("Loading " + f.getName());
            if(f.isDirectory() && f.listFiles().length > 0) { // Then we call the function recursively
                root.getChildren().add(getNodesForDirectory(f, false));
            } else {
                if (f.getName().endsWith(".mp3")) { // We are interested only in .mp3 files. 
                root.getChildren().add(new TreeItem<String>(f.getName()));
                }
            }
        }
        return root;
     }
     
     /*  
     We make use of the filenames inside TreeItem<String> to build path 
     from the selected TreeItem up to the root directory and return it.  
     Combining this path with root directory absolute path, 
     can give us an absolute path to the any .mp3 file, even when working with complicated file structure.
     */
     
     public static String findStarNode(TreeItem<String> parent, String toReturn) {

        if ( parent.getValue().contains(" \u2605")) {
            return "\\" + toReturn; 
        } else {
            return findStarNode(parent.getParent(), parent.getValue() + "\\" + toReturn);
        }

     }

     /* Searches for the TreeItem that contains exact filename.
        We use it to search File navigation menu (File View) for the last played track.      
     */
     public static TreeItem<String> findTreeItemByValue(TreeItem<String> root, String value) {
        if (root.getValue().equals(value)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = findTreeItemByValue(child, value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String getFileName(String lastPlayed) {
        //System.out.println("this is not the last separator "  + lastPlayed.substring(lastPlayed.lastIndexOf(File.separator) + 1));
        return lastPlayed.substring(lastPlayed.lastIndexOf(File.separator) + 1);
    }


    public static File universalFileGen(String filepath, String format) {

        try (InputStream in = Utils.class.getResourceAsStream(filepath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + filepath);
            }

            Path tempFile = Files.createTempFile("temp-resource", format);
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile.toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
