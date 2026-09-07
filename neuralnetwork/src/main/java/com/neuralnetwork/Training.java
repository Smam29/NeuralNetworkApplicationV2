package com.neuralnetwork;

import java.io.File;

public class Training {
    public static void startTraining(String[] args) {

        System.out.println("Program started");

        meshNetwork net = new meshNetwork(new int[]{784, 128, 64, 10});

        System.out.println("Network created");

        String testImages = mnistResource("t10k-images.idx3-ubyte");
        String testLabels = mnistResource("t10k-labels.idx1-ubyte");
        String trainImages = mnistResource("train-images.idx3-ubyte");
        String trainLabels = mnistResource("train-labels.idx1-ubyte");

        System.out.println("Test image path: " + testImages);
        System.out.println("Train image path: " + trainImages);

        System.out.println("First accuracy check");

        try{
            net.testingLoop(testImages, testLabels);
        } catch (Exception e) {
            System.out.println("Exception occurred:");
            e.printStackTrace();
        }

        System.out.println("Now training");


        try {
            net.trainingLoop(0.2, trainImages, trainLabels, 4);
        } catch (Exception e) {
            System.out.println("Exception occurred:");
            e.printStackTrace();
        }

        System.out.println("Second accuracy check");

        try{
            net.testingLoop(testImages, testLabels);
        } catch (Exception e) {
            System.out.println("Exception occurred:");
            e.printStackTrace();
        }

        System.out.println("Program finished");
    }

    private static String mnistResource(String fileName) {
        File file = new File("src/main/resources/" + fileName);
        if (file.exists()) {
            return file.getPath();
        }
        file = new File("target/classes/" + fileName);
        if (file.exists()) {
            return file.getPath();
        }
        throw new IllegalStateException("MNIST resource not found: " + fileName + ". Expected in src/main/resources or target/classes.");
    }
}
