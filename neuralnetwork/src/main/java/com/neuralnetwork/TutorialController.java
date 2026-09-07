package com.neuralnetwork;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

// controller for interactive tutorial showing how neural networks work
public class TutorialController implements Initializable {

    @FXML
    private Pane networkPane;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button backButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label tutorialText;
    
    List<Circle> inputNeurons = new ArrayList<>();
    List<Circle> hiddenNeurons = new ArrayList<>();
    List<Circle> outputNeurons = new ArrayList<>();
    List<Line> inputToHidden = new ArrayList<>();
    List<Line> hiddenToOutput = new ArrayList<>();
    //holds a list of tutorial steps
    private List<Runnable> steps = new ArrayList<>();
    private int currentStep = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        // builds visual network representation and sets up tutorial steps
        buildNetwork();
        setupTutorialSteps();
        resetNetwork();
        steps.get(0).run();
        updateButtons();
    }

    private Circle createNeuron(double x, double y) {
        Circle c = new Circle(20, Color.GRAY);
        c.setLayoutX(x);
        c.setLayoutY(y);
        networkPane.getChildren().add(c);
        return c;
    
    }

    private void buildNetwork(){
        double margin = 30;
        double paneHeight = networkPane.getPrefHeight();
        double paneWidth = networkPane.getPrefWidth();
        double inputX = paneWidth*0.2;
        double hiddenX = paneWidth/2;
        double outputX = paneWidth*0.8;
        double inputSpacing = paneHeight / 3.0;   
        double hiddenSpacing = paneHeight / 4.0;  
        double outputY = margin + paneHeight / 2.0;


        //input neurons       
        for (int i = 0; i < 2; i++) {
            double y = margin + inputSpacing * (i + 1);
            inputNeurons.add(createNeuron(inputX, y));
        }

        // hidden neurons
         for (int i = 0; i < 3; i++) {
            double y = margin + hiddenSpacing * (i + 1);
            hiddenNeurons.add(createNeuron(hiddenX, y));
        }

        //output neuron
        outputNeurons.add(createNeuron(outputX, outputY));

        generateConnections();
    }

    private void generateConnections(){
        for (Circle in : inputNeurons) {
            for (Circle hid : hiddenNeurons) {
                Line line = new Line(
                    in.getLayoutX(), in.getLayoutY(),
                    hid.getLayoutX(), hid.getLayoutY()
                );
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(2);

                inputToHidden.add(line);
                networkPane.getChildren().add(line);
                line.toBack();
            }
        }

        for (Circle hid : hiddenNeurons) {
            for (Circle out : outputNeurons) {
                Line line = new Line(
                    hid.getLayoutX(), hid.getLayoutY(),
                    out.getLayoutX(), out.getLayoutY()
                    );
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(2);

                hiddenToOutput.add(line);
                networkPane.getChildren().add(line);
                line.toBack();
            }
        }
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

    //when going back a tutorial step, reset the network to the original to prevent step bleeding
    private void resetNetwork() {

        inputNeurons.forEach(n -> n.setFill(Color.GRAY));
        hiddenNeurons.forEach(n -> n.setFill(Color.GRAY));
        outputNeurons.forEach(n -> n.setFill(Color.GRAY));

        inputToHidden.forEach(l -> l.setStroke(Color.LIGHTGRAY));
        hiddenToOutput.forEach(l -> l.setStroke(Color.LIGHTGRAY));
    }

    private void updateButtons() {
        backButton.setDisable(currentStep == 0);
        nextButton.setDisable(currentStep == steps.size() - 1);
    }

    public void goNext(ActionEvent event){
        if (currentStep < steps.size() - 1) {
            currentStep++;
            resetNetwork();
            steps.get(currentStep).run();
            updateButtons();
        }
    }

    public void goBack(ActionEvent event){
        if (currentStep > 0) {
            currentStep--;
            resetNetwork();
            steps.get(currentStep).run();
            updateButtons();
        }
    }

    private void setupTutorialSteps(){
        steps.add(() -> {
            inputNeurons.forEach(n -> n.setFill(Color.BLUE));
            tutorialText.setText("The circles highlighted above are input neurons.\nThey are responsible for passing values through to the other layers for processing.");
        });

        steps.add(() -> {
            inputToHidden.forEach(n -> n.setStroke(Color.BLUE));
            tutorialText.setText("The highlighted lines above represent the connections\n between the input neurons and the hidden layer neurons. \n Each connection has a weighted value.");
        });

        steps.add(() -> {
            hiddenNeurons.forEach(n -> n.setFill(Color.BLUE));
            tutorialText.setText("These highlighted circles are the neurons in the hidden layer.\n Networks can have multiple hidden layers.\n Every neuron that is not an input neuron has a bias.");
        });

        steps.add(() -> {
            inputNeurons.forEach(n -> n.setFill(Color.BLUE));
            inputToHidden.get(0).setStroke(Color.BLUE);
            inputToHidden.get(3).setStroke(Color.BLUE);
            hiddenNeurons.get(0).setFill(Color.BLUE);
            tutorialText.setText("The above shows all the connections connected to 1 hidden neuron.\n The input neurons values are multiplied by their corresponding connections weights\n and summed with the hidden neurons bias. The sum is then passed through an\n activation function which normalises the value into a range.");
         });

         steps.add(() -> {
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            tutorialText.setText("This highlighted circle is the output neuron, AKA a perceptron.\n In this simplified network there is only one neuron but for the networks in this\n application, there are 10 output neurons, one for each number.\n They hold the networks final prediction.");
         });

         steps.add(() -> {
            hiddenNeurons.forEach(n -> n.setFill(Color.BLUE));
            hiddenToOutput.forEach(n -> n.setStroke(Color.BLUE));
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            tutorialText.setText("The same process happens here as did between the input neurons\n and the hidden neurons, resulting in a final value that is normalised within a range.");
         });

         steps.add(() -> {
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            tutorialText.setText("The prediction in the output neuron is then compared to the correct answer.\n This gives an error value of 'how wrong is it'. The network can then backpropagate\n to update the weights and biases after each image.");
         });

         steps.add(() -> {
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            hiddenNeurons.forEach(n -> n.setFill(Color.BLUE));
            inputToHidden.forEach(n -> n.setStroke(Color.GREEN));
            hiddenToOutput.forEach(n -> n.setStroke(Color.GREEN));
            tutorialText.setText("Starting from the output layer, the error is propagated backwards through\n the network, calculating gradients for how sensitive the output is to each weight and bias\n and how they contribute to the final error.");
         });

         steps.add(() -> {
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            hiddenNeurons.forEach(n -> n.setFill(Color.BLUE));
            inputToHidden.forEach(n -> n.setStroke(Color.GREEN));
            hiddenToOutput.forEach(n -> n.setStroke(Color.GREEN));
            tutorialText.setText("Each weight and bias is adjusted in the direction that reduces error fastest,\n with the adjustment being scaled by the learning rate. Over multiple\n iterations, the network gradually improves its outputs to match the target output.");
         });

         steps.add(() -> {
            outputNeurons.forEach(n -> n.setFill(Color.BLUE));
            hiddenNeurons.forEach(n -> n.setFill(Color.BLUE));
            inputNeurons.forEach(n -> n.setFill(Color.BLUE));
            inputToHidden.forEach(n -> n.setStroke(Color.GREEN));
            hiddenToOutput.forEach(n -> n.setStroke(Color.GREEN));
            inputToHidden.get(0).setStroke(Color.TRANSPARENT);
            inputToHidden.get(4).setStroke(Color.TRANSPARENT);
            inputToHidden.get(5).setStroke(Color.TRANSPARENT);
            hiddenToOutput.get(1).setStroke(Color.TRANSPARENT);
            tutorialText.setText("The previous network shown was an example of a mesh network where all\n connections are present. Above is an example of a sparse network where some connections are\n missing. In this application sparsity is defined as the percentage of connections left\n e.g. a sparsity of 50 means half the connections are present.");
         });
    }
}
