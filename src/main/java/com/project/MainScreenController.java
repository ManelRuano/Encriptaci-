package com.project;

import javafx.fxml.FXML;

public class MainScreenController {

    @FXML
    private void initialize() {
        System.out.println("MainScreenController initialized");
    }

    @FXML
    private void openEncryptScreen() {
        UtilsViews.showView("EncryptScreen");
    }

    @FXML
    private void openDecryptScreen() {
        UtilsViews.showView("DecryptScreen");
    }
}
