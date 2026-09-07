package com.neuralnetwork;

import java.util.List;

// stores gradient values (derivatives) for updating network parameters during backpropagation
public class Gradient {
    public double[][] nabla_bias;
    public double[][][] nabla_mesh_weight;
    public List<Double>[][] nabla_sparse_weight;

    public Gradient(double[][] nabla_bias, double[][][] nabla_weight) {
        this.nabla_bias = nabla_bias;
        this.nabla_mesh_weight = nabla_weight;
    }

    public Gradient(double[][] nabla_bias, List<Double>[][] nabla_sparse_weight){
        this.nabla_bias = nabla_bias;
        this.nabla_sparse_weight = nabla_sparse_weight;
    }

}
