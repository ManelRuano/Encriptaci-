package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class DecryptScreenController {

    @FXML
    private TextField encryptedFileField;
    @FXML
    private TextField privateKeyField;
    @FXML
    private TextField outputFileField;

    public void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Encrypted File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Encrypted Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            encryptedFileField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void selectPrivateKeyFile() {
        if (privateKeyField != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Private Key File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Private Key Files", "*.key"));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                privateKeyField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    public void selectEncryptedFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Encrypted File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Encrypted Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            encryptedFileField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void decryptFile() throws Exception {
        try {
            // Read the private key from the selected file
            String privateKeyPath = privateKeyField.getText();
            String privateKey = readKeyFromFile(privateKeyPath);
    
            // Decrypt the file using the private key
            File encryptedFile = new File(encryptedFileField.getText());
            String decryptedFileName = outputFileField.getText();
            
            // Call the private decryptFile method and capture the decrypted content
            String decryptedContent = decryptFile(encryptedFile, privateKey, decryptedFileName);
    
            // After successful decryption, prompt the user to choose a location to save the decrypted file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Decrypted File");
            fileChooser.setInitialFileName(decryptedFileName); // Suggest the same file name
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                // Save the decrypted content to the chosen location
                Files.write(selectedFile.toPath(), decryptedContent.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            // Handle the exception appropriately (display an error message, log, etc.)
        }
    }

    private String readKeyFromFile(String keyPath) throws IOException {
        StringBuilder key = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(keyPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                key.append(line).append("\n");
            }
        }
        return key.toString();
    }

    private String decryptFile(File encryptedFile, String privateKey, String decryptedFileName) throws Exception {
        // Convert the private key string to PrivateKey object
        PrivateKey rsaPrivateKey = getRSAPrivateKey(privateKey);
    
        // Initialize the Cipher for decryption
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
    
        // Read the encrypted file, decrypt each line, and concatenate the results
        StringBuilder decryptedContent = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(encryptedFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                byte[] decodedEncryptedLine = Base64.getDecoder().decode(line);
                byte[] decryptedBytes = cipher.doFinal(decodedEncryptedLine);
                decryptedContent.append(new String(decryptedBytes, StandardCharsets.UTF_8))
                        .append(System.lineSeparator());
            }
        }
    
        // Print debug information
        System.out.println("Encrypted content:\n" + Files.readString(encryptedFile.toPath()));
        System.out.println("Decrypted content:\n" + decryptedContent);
    
        // Write the decrypted content to the file
        Path decryptedFilePath = Path.of(decryptedFileName);
        Files.write(decryptedFilePath, decryptedContent.toString().getBytes(StandardCharsets.UTF_8));
    
        // Return the decrypted content as a string
        return decryptedContent.toString();
    }

    private PrivateKey getRSAPrivateKey(String key) throws GeneralSecurityException {
        key = key.replaceAll("\\s", "");
    
        byte[] keyBytes = Base64.getDecoder().decode(key);
    
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    
        return keyFactory.generatePrivate(keySpec);
    }
    
    private void saveDecryptedFile(String decryptedContent, File outputFile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(outputFile);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(decryptedContent);
        }
    }
}
