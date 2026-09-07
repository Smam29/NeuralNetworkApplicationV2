package com.neuralnetwork;

// holds the output of a forward pass through the network
public class FeedForwardResult {
    public double[][] activations;
    public double[][] z_values;

    public FeedForwardResult(double[][] activations, double[][] z_values) {
        this.activations = activations;
        this.z_values = z_values;
    }

}
