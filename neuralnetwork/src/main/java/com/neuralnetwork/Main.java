package com.neuralnetwork;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
//import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
//import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args) throws SQLException {
        Database.sortConnection();
        launch(args);

    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root  = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene mainMenu = new Scene(root);
        String css = this.getClass().getResource("neuralnetwork.css").toExternalForm();
        mainMenu.getStylesheets().add(css);
        stage.setTitle("MLP Neural Networks");
        stage.setResizable(false);
        stage.setScene(mainMenu);
        stage.show();
    }

    @Override
    public void stop() throws Exception {

        Database.closeDatabase();
        super.stop();
    }
      
}
    