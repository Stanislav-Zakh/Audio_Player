package dev.staniszak.app;

public class App {
 
    /*
    Important 
    When building FAT jar with javafx GUI library, 
    you are required to have main class that does not extend javafx.application.Application.
    If after packaging application into the FAT jar and running it with java -jar ApplicationName.jar
    produces an Error: JavaFX runtime components are missing, and are required to run this application.
    You need create a wrapper main class (like this one) that does not extend javafx.application.Application. 
     */   
    public static void main(String[] args) {
        InnerApp.main(args);
    }

}
