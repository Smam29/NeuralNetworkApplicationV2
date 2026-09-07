package com.neuralnetwork;

// represents a single connection between two neurons with a weight value
public class networkConnection {
    public int inputIndex;
    public int outputIndex;
    public double weight;

    public networkConnection(int inputIndex, int outputIndex, double weight){
        this.inputIndex = inputIndex;
        this.outputIndex = outputIndex;
        this.weight = weight;
    }
}
