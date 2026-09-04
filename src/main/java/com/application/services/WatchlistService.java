package com.application.services;

import com.application.models.WatchlistItem;
import com.application.utils.WatchlistDialog;
import com.application.utils.WatchlistManager;
import javafx.scene.control.Alert;

public class WatchlistService {
    
    private final WatchlistManager manager;
    
    public WatchlistService(WatchlistManager manager) {
        this.manager = manager;
    }
    
    /**
     * Adaugă un simbol nou în watchlist cu validare
     */
    public void addSymbol() {
        WatchlistDialog.showAddDialog().ifPresent(item -> {
            // Verifică dacă există deja
            if (symbolExists(item.getSymbol())) {
                showErrorDialog("Symbol already exists", 
                    item.getSymbol() + " is already in your watchlist.");
                return;
            }
            
            // Adaugă în listă și salvează
            manager.addItem(item);
            System.out.println("✅ Added: " + item.getSymbol() + " - " + item.getCompany());
        });
    }
    
    /**
     * Șterge un simbol din watchlist
     */
    public void deleteSymbol(WatchlistItem item) {
        if (item == null) {
            showErrorDialog("No selection", "Please select a symbol to delete.");
            return;
        }
        
        if (WatchlistDialog.showDeleteConfirmation(item.getSymbol())) {
            manager.removeItem(item);
            System.out.println("🗑️ Removed: " + item.getSymbol());
        }
    }
    
    /**
     * Editează un simbol existent
     */
    public void editSymbol(WatchlistItem item) {
        if (item == null) {
            showErrorDialog("No selection", "Please select a symbol to edit.");
            return;
        }
        
        WatchlistDialog.showEditDialog(item).ifPresent(updated -> {
            // Verifică dacă noul simbol există deja (exceptând elementul curent)
            boolean exists = manager.getData().stream()
                .anyMatch(existing -> 
                    existing != item && 
                    existing.getSymbol().equalsIgnoreCase(updated.getSymbol())
                );
            
            if (exists) {
                showErrorDialog("Symbol already exists", 
                    updated.getSymbol() + " is already in your watchlist.");
                return;
            }
            
            manager.updateItem(item, updated);
            System.out.println("✏️ Updated: " + updated.getSymbol() + " - " + updated.getCompany());
        });
    }
    
    /**
     * Curăță toate simbolurile
     */
    public void clearAll() {
        if (manager.getData().isEmpty()) {
            showErrorDialog("List is empty", "There are no symbols to clear.");
            return;
        }
        
        if (WatchlistDialog.showClearAllConfirmation()) {
            manager.clearAll();
            System.out.println("🗑️ All items cleared");
        }
    }
    
    /**
     * Verifică dacă un simbol există deja
     */
    private boolean symbolExists(String symbol) {
        return manager.getData().stream()
            .anyMatch(existing -> existing.getSymbol().equalsIgnoreCase(symbol));
    }
    
    /**
     * Afișează un dialog de eroare
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}