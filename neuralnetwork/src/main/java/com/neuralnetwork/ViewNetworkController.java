package com.neuralnetwork;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// controller for viewing loaded network details and testing its performance
public class ViewNetworkController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private String name;
    private int sparsity;
    private double accuracy;
    private int loops_over;
    private double learning_rate;
    private String architecture;
    private Network network;
    @FXML
    private Label nameLabel;
     @FXML
    private Label typeLabel;
     @FXML
    private Label sparsityLabel;
     @FXML
    private Label layerLabel;
     @FXML
    private Label accuracyLabel;
     @FXML
    private Label loopLabel;
     @FXML
    private Label learnLabel;
    @FXML
    private Canvas networkCanvas;
    private GraphicsContext gc;
    @FXML
    private Button clearCanvas;
    @FXML
    private Button getNumber;
    @FXML
    private Label displayNumber;
    @FXML
    private Label confidenceLabel;

    @FXML
    public void initialize() throws SQLException {
        network = loadNetwork();
        nameLabel.setText("Network Name: " + name);
        typeLabel.setText("Network Type: " + architecture);
        if (sparsity == -1){
            sparsityLabel.setText("Sparsity: N/A");
        } else {
            sparsityLabel.setText("Sparsity: " + sparsity);
        }
        layerLabel.setText("Layers:" + network.getLayers());
        accuracyLabel.setText("Accuracy: " + accuracy);
        loopLabel.setText("Epochs: " + loops_over);
        learnLabel.setText("Learning Rate: " + learning_rate);
        drawCanvas();
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

    public Network loadNetwork() throws SQLException{
        Connection connection = Database.getConnection();
        String sql = "SELECT name, architecture, sparsity, accuracy, "
            + "loops_through, learning_rate, network_json FROM network WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, Database.getNetwork());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Network not found: " + Database.getNetwork());
                }

                architecture = rs.getString("architecture");
                String json = rs.getString("network_json");
                name = rs.getString("name");
                sparsity = rs.getInt("sparsity");
                accuracy = rs.getDouble("accuracy");
                loops_over = rs.getInt("loops_through");
                learning_rate = rs.getDouble("learning_rate");
                Gson gson = new Gson();

                if (architecture.equals("Mesh")) {
                    return gson.fromJson(json, meshNetwork.class);
                } else {
                    return gson.fromJson(json, sparseNetwork.class);
                }
            }
        }
    }

    public void drawCanvas(){
        gc = networkCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(28);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, networkCanvas.getWidth(), networkCanvas.getHeight());

        networkCanvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });

        networkCanvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });
    }

    public void clearCanvas(ActionEvent event){
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, networkCanvas.getWidth(), networkCanvas.getHeight());
    }

    public void getImage(ActionEvent event){
        getNumber.setDisable(true);
        WritableImage snapshot = networkCanvas.snapshot(null, null);
        Canvas smallCanvas = new Canvas(28, 28);
        GraphicsContext gcSmall = smallCanvas.getGraphicsContext2D();
        gcSmall.drawImage(snapshot, 0, 0, 28, 28);
        WritableImage scaled = smallCanvas.snapshot(null, null);

        PixelReader reader = scaled.getPixelReader();
        double[] input = new double[28 * 28];
        int index = 0;
        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                Color c = reader.getColor(x, y);
                input[index++] = 1.0 - c.getBrightness(); // black = 1, white = 0
            }
        }
        FeedForwardResult result = network.feedforward(input);
        int number = network.unvectoriseResult(result);
        double confidence = result.activations[result.activations.length - 1][number] * 100.0;
        String s = String.valueOf(number);
        displayNumber.setText(s);
        confidenceLabel.setText(String.format("Confidence: %.2f%%", confidence));
        getNumber.setDisable(false);
        
    }
    
}
