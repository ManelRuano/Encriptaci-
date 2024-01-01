package com.project;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import java.io.*;
import java.security.SecureRandom;

public class EncryptionUtil {

    public static void encryptFile(File inputFile, File publicKeyFile, File outputFile) {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            PGPPublicKey publicKey = readPublicKey(publicKeyFile);

            PGPUtil.writeFileToLiteralData(outputStream, PGPLiteralData.BINARY, inputFile);

            try (OutputStream encryptedOut = new ArmoredOutputStream(outputStream)) {
                PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
                        new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(true).setSecureRandom(new SecureRandom()).setProvider("BC")
                );
                encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey).setProvider("BC"));

                try (OutputStream encryptedStream = encryptedDataGenerator.open(encryptedOut, new byte[1 << 16])) {
                    PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
                    try (OutputStream compressedStream = compressedDataGenerator.open(encryptedStream)) {
                        PGPUtil.writeFileToLiteralData(compressedStream, PGPLiteralData.BINARY, inputFile);
                    }
                    compressedDataGenerator.close();
                }
                encryptedDataGenerator.close();
            }
        } catch (IOException | PGPException e) {
            e.printStackTrace();
        }
    }

    private static PGPPublicKey readPublicKey(File publicKeyFile) throws IOException, PGPException {
        try (InputStream keyIn = PGPUtil.getDecoderStream(new FileInputStream(publicKeyFile))) {
            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(keyIn);
            PGPPublicKeyRing pubKeyRing = (PGPPublicKeyRing) pgpFact.nextObject();

            if (pubKeyRing != null) {
                return pubKeyRing.getPublicKey();
            } else {
                throw new IllegalArgumentException("No encryption key found in the provided public key file.");
            }
        }
    }
}
