package com.neuralnetwork;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateNetworkController implements Initializable{
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private TextField nameTextField;
    @FXML
    private ChoiceBox<String> architectureType;
    private String[] architecture = {"Mesh Network", "Sparse Network"};
    @FXML
    private TextField sparsityTextField;
    @FXML 
    private Button createButton;
    String name;
    @FXML
    private Label confirmLabel;
    int sparsity;
    @FXML
    private TextField hiddenTextField;
    private int[] layers;
    @FXML
    private TextField loopsTextField;
    private int noLoops;
    @FXML
    private TextField learnTextField;
    private double learningRate;
    @FXML
    private ProgressBar progressBar;
    private Thread trainingThread;
    @FXML

    private double accuracy;

    String testImages = mnistResource("t10k-images.idx3-ubyte");
    String testLabels = mnistResource("t10k-labels.idx1-ubyte");
    String trainImages = mnistResource("train-images.idx3-ubyte");
    String trainLabels = mnistResource("train-labels.idx1-ubyte");

    //method for the return button
    public void returnToMenu(ActionEvent event) throws IOException{
        if (trainingThread != null && trainingThread.isAlive()) {
            trainingThread.interrupt();
            System.out.println("Training Interrupted");
        }
        root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String css = this.getClass().getResource("neuralnetwork.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }
    // method on pressing submit
    public void submit(ActionEvent event){
        try{
            //retrieve architecture type
            String currentA = architectureType.getValue();
            if (currentA == null || nameTextField.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Architecture and name are required");
            }
            layers = convertHiddenLayers(hiddenTextField.getText());
            for (int layerSize : layers) {
                if (layerSize <= 0) {
                    throw new IllegalArgumentException("Layer sizes must be positive");
                }
            }
            noLoops = Integer.parseInt(loopsTextField.getText());
            if (noLoops <= 0) {
                throw new IllegalArgumentException("Epochs must be positive");
            }
            learningRate = Double.parseDouble(learnTextField.getText());
            if (!Double.isFinite(learningRate) || learningRate <= 0) {
                throw new IllegalArgumentException("Learning rate must be positive and finite");
            }
            //for sparse - check all input fields are valid, train then test network then enter into database
            if (currentA.equals("Sparse Network")){
                name = nameTextField.getText();
                sparsity = Integer.parseInt(sparsityTextField.getText());
                if (sparsity < 0 || sparsity > 100) {
                    throw new IllegalArgumentException("Sparsity must be between 0 and 100");
                }
                sparseNetwork net = new sparseNetwork(layers, sparsity / 100.0);
                Stage targetStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                confirmLabel.setText("The network is training. \nThis may take a few minutes.");
                createButton.setDisable(true);
                trainNetwork(net, learningRate, () -> {

                    try {
                        accuracy = net.testingLoop(testImages, testLabels);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        Database.getDatabase().addNetwork(Database.getConnection(), net, name, "Sparse", sparsity, noLoops, learningRate, accuracy);
                        Platform.runLater(() -> showViewNetwork(targetStage));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showDatabaseSaveError();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        showDatabaseSaveError();
                    }
                });
            }
            //for mesh - check all input fields are valid, train then test network then enter into database
            if (currentA.equals("Mesh Network")){
                name = nameTextField.getText();
                meshNetwork net = new meshNetwork(layers);
                Stage targetStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                confirmLabel.setText("The network is training. \nThis may take a few minutes.");
                createButton.setDisable(true);
                trainNetwork(net, learningRate, () -> {
                    try {
                        accuracy = net.testingLoop(testImages, testLabels);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        Database.getDatabase().addNetwork(Database.getConnection(), net, name, "Mesh", -1, noLoops, learningRate, accuracy);
                        Platform.runLater(() -> showViewNetwork(targetStage));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showDatabaseSaveError();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        showDatabaseSaveError();
                    }
                });

            }
        }
        catch (Exception e) {
            confirmLabel.setText("One or more fields are invalid");
        }
    }

    private void showDatabaseSaveError() {
        Platform.runLater(() -> {
            confirmLabel.setText("Unable to save network: database connection failed");
            createButton.setDisable(false);
        });
    }

    private void showViewNetwork(Stage targetStage) {
        try {
            root = FXMLLoader.load(getClass().getResource("ViewNetwork.fxml"));
            scene = new Scene(root);
            String css = this.getClass().getResource("viewnetwork.css").toExternalForm();
            root.getStylesheets().add(css);
            targetStage.setScene(scene);
            targetStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        architectureType.getItems().addAll(architecture);
        architectureType.setOnAction(this::getArchitecture);
    }
    // updates ui when architecture selection changes - shows/hides sparsity field
    public void getArchitecture(ActionEvent event){
        String currentA = architectureType.getValue();
        // if sparsity is not required, clear it and grey it out to prevent inputs
        if (currentA.equals("Mesh Network")) {
            sparsityTextField.setDisable(true);
            sparsityTextField.clear();
        }
        else{
            sparsityTextField.setDisable(false);
        }
    }

    // take hidden layer input and fill in input and output neurons layers
    public int[] convertHiddenLayers(String s){ 
        int[] nums = Arrays.stream(s.split(","))
                   .map(String::trim)
                   .mapToInt(Integer::parseInt)
                   .toArray();

        int[] full = new int[nums.length + 2];
        full[0] = 784;
        for(int i = 0; i < nums.length; i++){
            full[i+1] = nums[i];
        }
        full[full.length-1] = 10;
        return full;
    }

    // load mnist resources
    private static String mnistResource(String fileName) {
        URL resource = CreateNetworkController.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalStateException("MNIST resource not found on the classpath: " + fileName);
        }

        try {
            return new File(resource.toURI()).getPath();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to access MNIST resource: " + fileName, e);
        }
    }

    // trains network on seperate thread, this enusres ui updates can still occur while it trains on a seperate thread
    public void trainNetwork(Network net, double learningRate, Runnable onFinished){
        trainingThread = new Thread(() -> {
            long trainingStart = System.nanoTime();
            try {
                System.out.println("Training");
                net.trainingLoop(learningRate, trainImages, trainLabels, noLoops);
            } catch (Exception e) {
                System.out.println("Exception occurred:");
                e.printStackTrace();
                return;
            } finally {
                long trainingDuration = System.nanoTime() - trainingStart;
                System.out.printf("Training duration: %.3f seconds%n", trainingDuration / 1_000_000_000.0);
            }
            onFinished.run();
            
        }, "network-training");
        trainingThread.setDaemon(true);
        trainingThread.start();

    }
    

}