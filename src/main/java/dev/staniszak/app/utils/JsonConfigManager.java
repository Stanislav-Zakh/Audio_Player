package dev.staniszak.app.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.staniszak.app.common.UserConfig;
import lombok.Getter;

/* Class works with files that are located in the working directory but not packaged inside a jar or exe.
 * Responsible for creating, reading, and writing files. 
 */

public class JsonConfigManager {
    @Getter 
    private static final String CONFIG_FILE_PATH = "data/configs/user.json"; // <- user settings
    @Getter 
    private static final String LIB_FILE_PATH = "data/default-audio-lib"; 

    private static ObjectMapper objectMapper = new ObjectMapper();

    /* We are using Jackson ObjectMapper to save and load user settings.*/
    public static UserConfig loadConfig() throws IOException {
        return objectMapper.readValue(new File(JsonConfigManager.getExternalFile(CONFIG_FILE_PATH)), UserConfig.class);
    }
    
    public static void saveConfig(UserConfig config) throws IOException {
        objectMapper.writeValue(new File(JsonConfigManager.getExternalFile(CONFIG_FILE_PATH)), config);
    }

    /* Create a file, and any parent directory, if the file does not exist.*/ 
    public static boolean initDefaultFile(String relativePath) {

        String fullPath = getExternalFile(relativePath);

        File file = new File(fullPath);
        if (!file.exists()) {
            try {
            file.getParentFile().mkdirs();    
            return file.createNewFile(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /* Create a directory, and any parent directory, if the directory does not exist. */ 
    public static boolean initDefaultDir(String relativePath) {

        String fullPath = getExternalFile(relativePath);

        File file = new File(fullPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();    
            return file.mkdir();
        }
        return false;
    }
    /* Get a file that is located in the current work directory but outside of JAR. 
       Note: (Everything under src/ is being packaged inside the JAR.) 
    */
    private static String getExternalFile(String relativePath) {
        // Get the current working directory
        String workingDir = System.getProperty("user.dir");
        // Construct the file path
        return Paths.get(workingDir, relativePath).toString();
    }

    /* Writes default config file. 
    The notation is a little verbose and prone to change so I have placed it inside a method.
    */ 
    public static void writeDefaultConfig() {

        if (initDefaultFile(CONFIG_FILE_PATH)) {
            try {
            JsonConfigManager.saveConfig(new UserConfig(JsonConfigManager.getExternalFile(LIB_FILE_PATH), ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
