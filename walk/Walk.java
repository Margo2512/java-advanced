package info.kgeorgiy.ja.kadochnikova.walk;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;

public class Walk {
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("noInputData");
            return;
        }
        if (args.length != 2) {
            System.err.println("lengthNotEqualTwo");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.err.println("noInputFirstOrSecondData");
            return;
        }
        Path inputPath;
        try {
            inputPath = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            System.err.println("InvalidPathException" + e.getMessage());
            return;
        }
        Path outputPath;
        try {
            outputPath = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("InvalidPathException" + e.getMessage());
            return;
        }
        if (!Files.exists(outputPath)) {
            Path file = Paths.get(args[1]);
            if (file.getParent() == null) {
                System.err.println("folderNotExist");
                return;
            }
            Path parent = file.getParent();
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                System.err.println("IOException" + e.getMessage());
                return;
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            Path path = Paths.get(args[1]);
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String fileName = line;
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    try (InputStream in = Files.newInputStream(Path.of(fileName))) {
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = in.read(buffer, 0, 8192)) > 0) {
                            digest.update(buffer, 0, count);
                        }
                        byte[] hash = digest.digest();
                        writer.write(String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash)) + " " + fileName + System.lineSeparator());
                    } catch (IOException | InvalidPathException | SecurityException e) {
                        writer.write("0".repeat(64) + " " + fileName + System.lineSeparator());
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("FileNotFoundException");
            } catch (IOException e) {
                System.err.println("IOException");
            } catch (SecurityException e) {
                System.err.println("SecurityException");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } catch (FileNotFoundException e) {
            System.err.println("FileNotFoundException");
        } catch (SecurityException e) {
            System.err.println("SecurityException");
        } catch (FileAlreadyExistsException e) {
            System.err.println("FileAlreadyExistsException");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
