package com.neuralnetwork;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ModelInfo {
    private final String name;
    private final String arch;
    private final double accuracy;
    private final int id;
    

    public ModelInfo(int id, String name, String arch, double accuracy){
        this.name = name;
        this.arch = arch;
        this.accuracy = accuracy;
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getArch(){
        return arch;
    }

    public double getAccuracy(){
        return accuracy;
    }

    @Override
    public String toString(){
        return name + " | Type: " + arch + " | Accuracy: " + accuracy;
    }

    public static List<ModelInfo> loadModelsFromDatabase() {
        // retrieves all saved networks from database and creates modelinfo objects
        List<ModelInfo> list = new ArrayList<>();

        String sql = "SELECT id, name, architecture, accuracy FROM NETWORK";

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String arch = rs.getString("architecture");
                Double accuracy = rs.getDouble("accuracy");

                list.add(new ModelInfo(id, name, arch, accuracy));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
}

}
