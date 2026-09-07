package com.neuralnetwork;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchToTutorial(ActionEvent event) throws IOException{
        root = FXMLLoader.load(getClass().getResource("Tutorial.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("tutorial.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToNewNetwork(ActionEvent event) throws IOException{
        root = FXMLLoader.load(getClass().getResource("NewNetwork.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("createnetwork.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToLoadNetwork(ActionEvent event) throws IOException{
        root = FXMLLoader.load(getClass().getResource("LoadNetwork.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("loadnetwork.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }
    
}
