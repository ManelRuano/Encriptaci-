package com.project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;

public class EncryptScreenController {

    @FXML
    public TextField publicKeyField;
    @FXML
    public TextField fileField;
    @FXML
    public TextField destinationField;

    public void selectPublicKeyFile() {
        if (publicKeyField != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Public Key File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Public Key Files", "*.pub"));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                publicKeyField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    public void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void encryptFile() {
        try {
            // Read the public key from the selected file
            String publicKeyPath = publicKeyField.getText();
            String publicKey = readKeyFromFile(publicKeyPath);
    
            // Encrypt the file using the public key
            File inputFile = new File(fileField.getText());
            String encryptedFileName = destinationField.getText();
    
            // Initialize the Cipher for encryption
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, getRSAPublicKey(publicKey));
    
            // Read the input file line by line, encrypt each line, and concatenate the results
            StringBuilder encryptedContent = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(inputFile.toPath())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    byte[] encryptedBytes = cipher.doFinal(line.getBytes(StandardCharsets.UTF_8));
                    String encodedEncryptedLine = Base64.getEncoder().encodeToString(encryptedBytes);
                    encryptedContent.append(encodedEncryptedLine).append(System.lineSeparator());
                }
            }
    
            // Print debug information
            System.out.println("Original content:\n" + Files.readString(inputFile.toPath()));
            System.out.println("Encrypted content:\n" + encryptedContent);
    
            // Write the encrypted content to the file
            Path encryptedFilePath = Paths.get(encryptedFileName);
            Files.write(encryptedFilePath, encryptedContent.toString().getBytes(StandardCharsets.UTF_8));
    
            // Prompt the user to choose a location to save the encrypted file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(encryptedFileName);
            fileChooser.setTitle("Save Encrypted File");
            File selectedFile = fileChooser.showSaveDialog(null);
    
            if (selectedFile != null) {
                Files.copy(encryptedFilePath, selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File saved successfully: " + selectedFile.getAbsolutePath());
            } else {
                System.out.println("File not saved.");
            }
    
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // Handle the exception appropriately (display an error message, log, etc.)
        }
    }

    private String readKeyFromFile(String keyPath) throws IOException {
        StringBuilder key = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(keyPath))) {
            while (scanner.hasNext()) {
                key.append(scanner.nextLine()).append("\n");
            }
        }
        return key.toString();
    }

    private PublicKey getRSAPublicKey(String key) throws GeneralSecurityException {
        key = key.replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

    private void saveEncryptedFile(File inputFile, File outputFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}