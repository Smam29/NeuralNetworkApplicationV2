package com.neuralnetwork;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;


public class LoadNetworkController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Button loadButton;
    @FXML
    private Button deleteButton;
    @FXML
    private ListView<ModelInfo> networkList;

    @FXML
    public void initialize(){
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
        List<ModelInfo> models = ModelInfo.loadModelsFromDatabase();
        networkList.getItems().addAll(models);
        networkList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadButton.setDisable(false);
                deleteButton.setDisable(false);
                Database.setNetwork(newVal.getId());
            }
        });
    }

    public void returnToMenu(ActionEvent event) throws IOException{

        root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("neuralnetwork.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onDeleteClicked(ActionEvent event) {
        ModelInfo selected = networkList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return; // nothing selected
        }

        int id = selected.getId();

        // delete from DB
        if (!Database.deleteNetworkById(id)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unable to Load Network");
            alert.setHeaderText("The network could not be deleted");
            alert.setContentText("Unable to delete the network because the database connection failed.");
            alert.showAndWait();
            return;
        }

        // remove from ListView
        networkList.getItems().remove(selected);
        networkList.getSelectionModel().clearSelection();
        networkList.refresh();

        // grey out buttons again
        loadButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void onLoadClicked(ActionEvent event) throws IOException{
        try {
            root = FXMLLoader.load(getClass().getResource("ViewNetwork.fxml"));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unable to Load Network");
            alert.setHeaderText("The network could not be loaded");
            alert.setContentText("Unable to load the network because the database connection failed.");
            alert.showAndWait();
            return;
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("viewnetwork.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }




}