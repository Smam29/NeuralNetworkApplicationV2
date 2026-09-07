package com.neuralnetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class sparseNetwork implements Network{
    private int numLayers;
    private int[] layerSizes;
    private double[][] biases;
    private List<networkConnection>[][] sparseWeightsForward;
    private List<networkConnection>[][] sparseWeightsBack;

    @SuppressWarnings("unchecked")
    public sparseNetwork(int[] layerSizes, double sparsity){
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
        // creating the weights using dynamic list sizing
        sparseWeightsForward = new ArrayList[numLayers-1][];
        sparseWeightsBack = new ArrayList[numLayers-1][];

        for(int layer = 0; layer < numLayers-1; layer++){
            sparseWeightsForward[layer] = new ArrayList[layerSizes[layer+1]];
            sparseWeightsBack[layer] = new ArrayList[layerSizes[layer]];

            for(int nextLayerNeuron = 0; nextLayerNeuron < layerSizes[layer+1]; nextLayerNeuron++){
                sparseWeightsForward[layer][nextLayerNeuron] = new ArrayList<>();
            }

            // maintain backward references for efficient backpropagation
            for(int prevLayerNeuron = 0; prevLayerNeuron < layerSizes[layer]; prevLayerNeuron++){
                sparseWeightsBack[layer][prevLayerNeuron] = new ArrayList<>();
            }

            for(int nextLayerNeuron = 0; nextLayerNeuron < layerSizes[layer+1]; nextLayerNeuron++){

        // if random double is lower 
                for(int prevLayerNeuron = 0; prevLayerNeuron < layerSizes[layer]; prevLayerNeuron++){
                    if (rand.nextDouble() < sparsity){
                        networkConnection connection = new networkConnection(prevLayerNeuron, nextLayerNeuron, rand.nextGaussian());
                        sparseWeightsForward[layer][nextLayerNeuron].add(connection);
                        sparseWeightsBack[layer][prevLayerNeuron].add(connection);
                    }
                }
            }
        }
    }

    @Override
    public double getSigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    @Override
    public double getSigmoidPrime(double x) {
        return getSigmoid(x)*(1-getSigmoid(x));
    }

    @Override
    public FeedForwardResult feedforward(double[] a) {
        // forward pass using only sparse connections
        double[][] activations = new double[numLayers][];
        double[][] z_values = new double[numLayers-1][];
        activations[0] = a.clone();

        for(int layer = 0; layer < numLayers-1; layer++){
            double[] z = new double[layerSizes[layer+1]];

            for(int nextLayerNeuron = 0; nextLayerNeuron < layerSizes[layer + 1]; nextLayerNeuron++){
                double sum = 0.0;

                // compute weighted sum only for existing sparse connections
                for(networkConnection c : sparseWeightsForward[layer][nextLayerNeuron]){
                    sum += c.weight * a[c.inputIndex];
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

    @SuppressWarnings("unchecked")
    @Override
    public Gradient backProp(double[] inputArray, double[] desiredOutput) {
        // computes gradients for sparse network using backpropagation
        double[][] nabla_bias = new double[numLayers - 1][];
        List<Double>[][] nabla_weight = new ArrayList[numLayers-1][];
        for (int i = 0; i < numLayers-1; i++){
            nabla_bias[i] = new double[layerSizes[i+1]];
            nabla_weight[i] = new ArrayList[layerSizes[i + 1]];
            for (int j = 0; j < layerSizes[i+1]; j++){
                nabla_weight[i][j] = new ArrayList<>();
            }
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
        List<Double>[] nabla_w_L = new ArrayList[size];
        for(int outNeuron = 0; outNeuron < size; outNeuron++){
            nabla_w_L[outNeuron] = new ArrayList<>();
            List<networkConnection> conns = sparseWeightsForward[L - 1][outNeuron];
            for(networkConnection c : conns){
                double grad = delta[outNeuron] * prevActivations[c.inputIndex];
                nabla_w_L[outNeuron].add(grad);
            }
        }
        nabla_weight[L-1] = nabla_w_L;
        for(int layer = L-1; layer >=1; layer--){
            double[] z = z_values[layer-1];
            double[] sp = new double[z.length];
            for (int i = 0; i < z.length; i++) {
                sp[i] = getSigmoidPrime(z[i]);
            }
            double newDelta[] = new double[layerSizes[layer]];

            for (int prevNeuron = 0; prevNeuron < layerSizes[layer]; prevNeuron++){
                double sum = 0.0;
                for (networkConnection c : sparseWeightsBack[layer][prevNeuron]){
                    sum += c.weight * delta[c.outputIndex];
                }
                newDelta[prevNeuron] = sum * sp[prevNeuron];
            }

            delta = newDelta;
            nabla_bias[layer-1] = delta;

            double[] prevA = activations[layer-1];
            List<Double>[] nabla_w_layer = new ArrayList[layerSizes[layer]];
            for (int outNeuron = 0; outNeuron < layerSizes[layer]; outNeuron++){
                nabla_w_layer[outNeuron] = new ArrayList<>();
                for (networkConnection c : sparseWeightsForward[layer-1][outNeuron]){
                    double grad = delta[outNeuron] * prevA[c.inputIndex];
                    nabla_w_layer[outNeuron].add(grad);
                }
            }
            nabla_weight[layer-1] = nabla_w_layer;
        }
            
        
        return new Gradient(nabla_bias, nabla_weight);
    }

    @Override
    public void updateParameters(Gradient gradient, double learningRate) {
        for (int layer = 0; layer < numLayers - 1; layer++) {
            for (int i = 0; i < biases[layer].length; i++){
                biases[layer][i] -= learningRate * gradient.nabla_bias[layer][i];
            }
            for (int outNeuron = 0; outNeuron < sparseWeightsForward[layer].length; outNeuron++){
                List<networkConnection> connections = sparseWeightsForward[layer][outNeuron];
                List<Double> grads = gradient.nabla_sparse_weight[layer][outNeuron];

                for (int j = 0; j < connections.size(); j++){
                    connections.get(j).weight -= learningRate * grads.get(j);
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
    public int unvectoriseResult(FeedForwardResult testResult) {
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
