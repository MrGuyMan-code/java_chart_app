package com.application.utils;

import com.application.models.WatchlistItem;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Window;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WatchlistStorage {
    
    private static final String HEADER = "Symbol,Company\n";
    private static String lastExportedPath = null;
    
    /**
     * Importă dintr-un fișier CSV selectat de utilizator
     * Gestionează tot: FileChooser, citire, confirmări (Replace/Add/Cancel)
     * @return Lista de WatchlistItem importate sau null dacă utilizatorul a anulat
     */
    public static List<WatchlistItem> importFromFile(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Watchlist");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return null; // Utilizatorul a anulat
        }
        
        List<WatchlistItem> items = loadFromFile(file.getAbsolutePath());
        if (items.isEmpty()) {
            showError("Import Failed", "No valid data found in the file.");
            return null;
        }
        
        lastExportedPath = file.getAbsolutePath();
        System.out.println("✅ Imported " + items.size() + " items from: " + file.getAbsolutePath());
        return items;
    }
    
    /**
     * Importă dintr-un fișier CSV și gestionează conflictul cu lista existentă
     * @param existingItems Lista existentă (pentru a verifica dacă e goală)
     * @param owner Fereastra părinte
     * @return Un obiect ImportResult care conține items și acțiunea dorită
     */
    public static ImportResult importWithConflictHandling(List<WatchlistItem> existingItems, Window owner) {
        // Pasul 1: Importă din fișier
        List<WatchlistItem> importedItems = importFromFile(owner);
        
        if (importedItems == null) {
            return new ImportResult(null, ImportAction.CANCEL);
        }
        
        // Pasul 2: Dacă lista existentă e goală, adaugă direct
        if (existingItems == null || existingItems.isEmpty()) {
            return new ImportResult(importedItems, ImportAction.REPLACE);
        }
        
        // Pasul 3: Dacă lista existentă nu e goală, întreabă utilizatorul
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Import Watchlist");
        alert.setHeaderText("Current watchlist has " + existingItems.size() + " items");
        alert.setContentText("Do you want to replace current watchlist or add to it?");
        
        ButtonType replaceButton = new ButtonType("Replace");
        ButtonType addButton = new ButtonType("Add");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(replaceButton, addButton, cancelButton);
        
        var result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == replaceButton) {
                return new ImportResult(importedItems, ImportAction.REPLACE);
            } else if (result.get() == addButton) {
                return new ImportResult(importedItems, ImportAction.ADD);
            }
        }
        
        return new ImportResult(null, ImportAction.CANCEL);
    }
    
    /**
     * Exportă într-un fișier CSV la locația aleasă de utilizator
     * Gestionează tot: FileChooser, salvare, mesaje
     * @return true dacă exportul a reușit, false dacă a fost anulat sau a eșuat
     */
    public static boolean exportToFile(List<WatchlistItem> items, Window owner) {
        if (items == null || items.isEmpty()) {
            showWarning("Export Warning", "Watchlist is empty. Nothing to export.");
            return false;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Watchlist");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("watchlist_" + System.currentTimeMillis() + ".csv");
        
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return false; // Utilizatorul a anulat
        }
        
        boolean success = saveToFile(items, file.getAbsolutePath());
        if (success) {
            lastExportedPath = file.getAbsolutePath();
            showInfo("Export Successful", 
                "✅ " + items.size() + " items exported to:\n" + file.getAbsolutePath());
        }
        return success;
    }
    
    /**
     * Salvează în ultima locație folosită
     * @return true dacă salvarea a reușit, false dacă a eșuat sau nu există locație
     */
    public static boolean saveToLastLocation(List<WatchlistItem> items, Window owner) {
        if (items == null || items.isEmpty()) {
            showWarning("Save Warning", "Watchlist is empty. Nothing to save.");
            return false;
        }
        
        if (lastExportedPath == null) {
            // Dacă nu s-a exportat niciodată, deschide dialogul de export
            return exportToFile(items, owner);
        }
        
        boolean success = saveToFile(items, lastExportedPath);
        if (success) {
            showInfo("Save Successful", 
                "✅ " + items.size() + " items saved to:\n" + lastExportedPath);
        }
        return success;
    }
    
    /**
     * Încarcă dintr-un fișier specificat
     */
    public static List<WatchlistItem> loadFromFile(String filePath) {
        List<WatchlistItem> items = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            System.err.println("❌ File not found: " + filePath);
            return items;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String symbol = parts[0].trim();
                    String company = parts[1].trim();
                    if (!symbol.isEmpty() && !company.isEmpty()) {
                        items.add(new WatchlistItem(symbol, company));
                    }
                }
            }
            System.out.println("✅ Loaded " + items.size() + " items from: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error loading from file: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Salvează într-un fișier specificat
     */
    public static boolean saveToFile(List<WatchlistItem> items, String filePath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(HEADER);
            for (WatchlistItem item : items) {
                writer.write(item.getSymbol() + "," + item.getCompany() + "\n");
            }
            System.out.println("✅ Saved " + items.size() + " items to: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving to file: " + e.getMessage());
            showError("Save Error", "Could not save to file:\n" + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obține ultima cale de export folosită
     */
    public static String getLastExportedPath() {
        return lastExportedPath;
    }
    
    /**
     * Resetează ultima cale de export
     */
    public static void resetLastExportedPath() {
        lastExportedPath = null;
    }
    
    // ===== CLASE AUXILIARE =====
    
    /**
     * Rezultatul unei operații de import
     */
    public static class ImportResult {
        private final List<WatchlistItem> items;
        private final ImportAction action;
        
        public ImportResult(List<WatchlistItem> items, ImportAction action) {
            this.items = items;
            this.action = action;
        }
        
        public List<WatchlistItem> getItems() {
            return items;
        }
        
        public ImportAction getAction() {
            return action;
        }
        
        public boolean isCanceled() {
            return action == ImportAction.CANCEL;
        }
        
        public boolean isReplace() {
            return action == ImportAction.REPLACE;
        }
        
        public boolean isAdd() {
            return action == ImportAction.ADD;
        }
    }
    
    /**
     * Acțiuni posibile la import
     */
    public enum ImportAction {
        REPLACE,
        ADD,
        CANCEL
    }
    
    // ===== METODE UTILITARE =====
    
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}