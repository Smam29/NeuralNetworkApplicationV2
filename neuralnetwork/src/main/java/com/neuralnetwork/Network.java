package com.neuralnetwork;

import java.io.IOException;

// interface defining the contract for all neural network types (mesh and sparse)
public interface Network {
    double getSigmoid(double x);
    double getSigmoidPrime(double x);
    FeedForwardResult feedforward(double[] a);
    Gradient backProp(double[] inputArray, double[] desiredOutput);
    void updateParameters(Gradient gradient, double learningRate);
    void trainingLoop(double learningRate, String imagePath, String labelPath, int epochs) throws IOException;
    int unvectoriseResult(FeedForwardResult testResult);
    double testingLoop(String imagePath, String labelPath) throws IOException;
    String getLayers();
}
