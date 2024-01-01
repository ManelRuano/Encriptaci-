package com.project;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UtilsViews {

    public static StackPane parentContainer = new StackPane();
    private static Map<String, Parent> views = new HashMap<>();

    public static void addView(Class<?> clazz, String name, String resource) {
        try {
            FXMLLoader loader = new FXMLLoader(clazz.getResource(resource));
            Parent view = loader.load();
            views.put(name, view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showView(String name) {
        parentContainer.getChildren().clear();
        parentContainer.getChildren().add(views.get(name));
    }

    public static Parent getView(String name) {
        return views.get(name);
    }
}
