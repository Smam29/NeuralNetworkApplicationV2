package com.neuralnetwork;

import java.util.Random;
import java.io.*;

public class meshNetwork implements Network {
    private int numLayers;
    private int[] layerSizes;
    private double[][] biases;
    private double[][][] weights;

    public meshNetwork(int[] layerSizes) {
        this.numLayers = layerSizes.length;
        this.layerSizes = layerSizes;
        Random rand = new Random();
        biases = new double[numLayers - 1][];
        for (int i = 1; i < numLayers; i++) {
            biases[i - 1] = new double[layerSizes[i]];
            for (int j = 0; j < layerSizes[i]; j++) {
                biases[i - 1][j] = rand.nextGaussian();
            }
        }
        weights = new double[numLayers - 1][][];
        for (int i = 0; i < numLayers - 1; i++) {
            int nextLayerNeurons = layerSizes[i + 1];
            int prevLayerNeurons = layerSizes[i];
            weights[i] = new double[nextLayerNeurons][prevLayerNeurons];
            for (int n = 0; n < nextLayerNeurons; n++) {
                for (int p = 0; p < prevLayerNeurons; p++) {
                    weights[i][n][p] = rand.nextGaussian();
                }
            }
        }
    }

    @Override
    public double getSigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    @Override
    public double getSigmoidPrime(double x){
        return getSigmoid(x)*(1-getSigmoid(x));
    }
    @Override
    public FeedForwardResult feedforward(double[] a) {
        double[][] activations = new double[numLayers][];
        double[][] z_values = new double[numLayers-1][];
        activations[0] = a.clone();
        for (int layer = 0; layer < numLayers - 1; layer++) {
            double[] z = new double[layerSizes[layer + 1]];
            for (int nextLayerNeuron = 0; nextLayerNeuron < layerSizes[layer + 1]; nextLayerNeuron++) {
                double sum = 0.0;
                for (int prevLayerNeuron = 0; prevLayerNeuron < layerSizes[layer]; prevLayerNeuron++) {
                        sum += weights[layer][nextLayerNeuron][prevLayerNeuron] * a[prevLayerNeuron];
                }
                z[nextLayerNeuron] = sum + biases[layer][nextLayerNeuron];
            }
            z_values[layer] = z.clone();
            for (int i = 0; i < z.length; i++) {
                z[i] = getSigmoid(z[i]);
            }
            activations[layer+1] = z;
            a = z;
        }
        return new FeedForwardResult (activations, z_values);
    }
    
    public double costFunction(double[] a, double[] y){
        double sum = 0.0;
        for (int outNeuron = 0; outNeuron < a.length; outNeuron++){
            double diff = y[outNeuron]-a[outNeuron];
            sum += (diff*diff)/2;
        }
        return sum;
    }
    @Override
    public Gradient backProp(double[] inputArray, double[] desiredOutput){
        double[][] nabla_bias = new double[numLayers - 1][];
        double[][][] nabla_weight = new double[numLayers - 1][][];
        for (int i = 0; i < numLayers-1; i++){
            nabla_bias[i] = new double[layerSizes[i+1]];
            nabla_weight[i] = new double[layerSizes[i+1]][layerSizes[i]];
        }
        FeedForwardResult activationObject = feedforward(inputArray);
        double[][] activations = activationObject.activations;
        double[][] z_values = activationObject.z_values;
        int L = numLayers-1;
        int size = activations[L].length;
        double[] delta = new double[size];
        for(int i = 0; i < size; i++){
            double a = activations[L][i];
            double target = desiredOutput[i];
            double costDeriv = a - target;
            double sPrime = getSigmoidPrime(z_values[L-1][i]);
            delta[i] = costDeriv * sPrime;
        }
        nabla_bias[L-1] = delta;
        double[] prevActivations = activations[L-1];
        double[][] nabla_w_L = new double[size][prevActivations.length];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < prevActivations.length; j++){
                nabla_w_L[i][j] = delta[i] * prevActivations[j];
            }
        }
        nabla_weight[L-1] = nabla_w_L;
        for(int layer = L-1; layer>=1; layer--){
            double[] z = z_values[layer-1];
            double[] sp = new double[z.length];
            for (int i = 0; i < z.length; i++) {
                sp[i] = getSigmoidPrime(z[i]);
            }
            double newDelta[] = new double[layerSizes[layer]];
            double weightsNext[][] = weights[layer];
            for(int i = 0; i < newDelta.length; i++){
                double sum = 0.0;
                for(int j = 0; j < delta.length; j++){
                    sum+= weightsNext[j][i] * delta[j];
                }
                newDelta[i] = sum * sp[i];
            }
            delta = newDelta;
            nabla_bias[layer-1] = delta; 
            double[] prevA = activations[layer-1];
            double[][] nabla_w_layer = new double[delta.length][prevA.length];
            for (int i = 0; i < delta.length; i++){
                for (int j = 0; j < prevA.length; j++){
                    nabla_w_layer[i][j] = delta[i] * prevA[j];
                }
            
            }
            nabla_weight[layer-1] = nabla_w_layer;
        }
        return new Gradient(nabla_bias, nabla_weight);
    }
    @Override
    public void updateParameters(Gradient gradient, double learningRate){
        for (int layer = 0; layer < numLayers - 1; layer++) {
            for (int i = 0; i < biases[layer].length; i++){
                biases[layer][i] -= learningRate * gradient.nabla_bias[layer][i];
            }
            for (int i = 0; i < weights[layer].length; i++){
                for (int j = 0; j < weights[layer][i].length; j++){
                    weights[layer][i][j] -= learningRate * gradient.nabla_mesh_weight[layer][i][j];
                }
            }
        }
    }
    @Override
    public void trainingLoop(double learningRate, String imagePath, String labelPath, int epochs) throws IOException{
        double [][] images = MNISTLoader.loadImages(imagePath);
        int [] labels = MNISTLoader.loadLabels(labelPath);
        if (images.length != labels.length) {
                throw new IllegalStateException("Number of images and labels do not match");
        }
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int i = 0; i < images.length; i++){
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                double[] label = MNISTLoader.vectoriseLabel(labels[i]);
                updateParameters(backProp(images[i], label), learningRate);
            }
        }
        System.out.println("Training complete");
    }
    @Override
    public int unvectoriseResult(FeedForwardResult testResult){
        double[] finalActivations = testResult.activations[numLayers-1];
        int number = 0;
        for(int i = 1; i < finalActivations.length; i++){
            if (finalActivations[i] > finalActivations[number]){
                number = i;
            }
        }
        return number;
    }
    @Override
    public double testingLoop(String imagePath, String labelPath) throws IOException{
        double [][] images = MNISTLoader.loadImages(imagePath);
        int [] labels = MNISTLoader.loadLabels(labelPath);
        double correctSum = 0;
        double accuracy;
        if (images.length != labels.length) {
                throw new IllegalStateException("Number of images and labels do not match");
        }
        for (int i = 0; i < images.length; i++){
            int x;
            x = unvectoriseResult(feedforward(images[i]));
            if (x == labels[i]){
                correctSum++;
            }
        }
        accuracy = correctSum/labels.length;
        System.out.println(accuracy);
        return accuracy;
    }

    @Override
    public String getLayers(){
        String layerPrint = " ";
        for(int i = 0; i  < numLayers; i++){
            int temp = layerSizes[i];
            layerPrint = layerPrint + " " + temp;
        }
        return layerPrint;
    }
}
