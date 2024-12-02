package com.ovi.zip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipCompressorExtractor extends JFrame {


    private static final long serialVersionUID = 1L;
    private JLabel titleLabel;
    private JButton compressButton;
    private JButton extractButton;

    public ZipCompressorExtractor() {
        setTitle("ZIP Compressor & Extractor");
        setSize(400, 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        titleLabel = new JLabel("Choose an option:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        compressButton = new JButton("Compress Files");
        extractButton = new JButton("Extract ZIP");

        compressButton.addActionListener(new CompressListener());
        extractButton.addActionListener(new ExtractListener());

        buttonPanel.add(compressButton);
        buttonPanel.add(extractButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private class CompressListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                JFileChooser saveFileChooser = new JFileChooser();
                saveFileChooser.setDialogTitle("Save ZIP File");
                saveFileChooser.setSelectedFile(new File("compressed.zip"));
                int saveResult = saveFileChooser.showSaveDialog(null);

                if (saveResult == JFileChooser.APPROVE_OPTION) {
                    File zipFile = saveFileChooser.getSelectedFile();

                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                        for (File file : selectedFiles) {
                            addToZip(file, zos);
                        }
                        JOptionPane.showMessageDialog(null, "Files compressed successfully!\nSaved to: " + zipFile.getAbsolutePath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                }
            }
        }

        private void addToZip(File file, ZipOutputStream zos) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    private class ExtractListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select ZIP File to Extract");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File zipFile = fileChooser.getSelectedFile();

                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("Choose Parent Directory for Extraction");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int folderResult = folderChooser.showOpenDialog(null);

                if (folderResult == JFileChooser.APPROVE_OPTION) {
                    File parentDir = folderChooser.getSelectedFile();
                    String folderName = zipFile.getName().substring(0, zipFile.getName().lastIndexOf('.'));
                    File destDir = new File(parentDir, folderName);

                    try {
                        unzip(zipFile, destDir.toPath());
                        JOptionPane.showMessageDialog(null, "Files extracted successfully!\nSaved to: " + destDir.getAbsolutePath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                    }
                }
            }
        }

        private void unzip(File zipFile, Path destDir) throws IOException {
            Files.createDirectories(destDir);
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path newFile = destDir.resolve(zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        try (FileOutputStream fos = new FileOutputStream(newFile.toFile())) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = zis.read(buffer)) >= 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    zis.closeEntry();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ZipCompressorExtractor app = new ZipCompressorExtractor();
            app.setVisible(true);
        });
    }
}
