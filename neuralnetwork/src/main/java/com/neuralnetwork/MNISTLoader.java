package com.neuralnetwork;

import java.io.*;

public class MNISTLoader {

    private static int numImages;
    public static double[][] loadImages(String path) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(path))){

        int magic = in.readInt();
        if (magic != 2051){
            throw new IOException("Invalid magic number: " + magic);
        }
        numImages = in.readInt();
        int numRows = in.readInt();
        int numCols = in.readInt();

        double[][] images = new double[numImages][numRows * numCols];

        for (int i = 0; i < numImages; i++) {
            for (int j = 0; j < numRows * numCols; j++) {
                images[i][j] = (in.readUnsignedByte() / 255.0);
            }
        }

        return images;
        }
    }

    public static int[] loadLabels(String path) throws IOException {
        // read binary mnist label file containing correct digit for each image
        try(DataInputStream in = new DataInputStream(new FileInputStream(path))){

            int magic = in.readInt();
            if (magic != 2049) throw new IOException("Invalid magic number: " + magic);

            int numLabels = in.readInt();
            int[] labels = new int[numLabels];

            for (int i = 0; i < numLabels; i++) {
                labels[i] = in.readUnsignedByte();
            }
            return labels;
        }
    }

    public static int getNumImages(){
        return numImages;
    }

    public static double[] vectoriseLabel(int label){
        // converts a digit (0-9) into a vector where only the correct position is 1
        double[] vectorisedLabel = new double[10];
        vectorisedLabel[label] = 1;
        return vectorisedLabel;
    } 
}
