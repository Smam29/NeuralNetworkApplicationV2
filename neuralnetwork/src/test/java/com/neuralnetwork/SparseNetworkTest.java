package com.neuralnetwork;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SparseNetworkTest {

    @Test
    public void backPropShouldInitializeGradientLists() {
        sparseNetwork network = new sparseNetwork(new int[]{2, 3, 1}, 0.5);

        Gradient gradient = network.backProp(new double[]{0.1, 0.2}, new double[]{0.9});

        assertNotNull(gradient);
        assertNotNull(gradient.nabla_sparse_weight);
    }
}
