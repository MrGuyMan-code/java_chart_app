package com.application.utils;

import com.application.models.WatchlistItem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;

import java.util.Optional;

public class WatchlistDialog {
    
    /**
     * Dialog pentru adăugare simbol nou
     */
    public static Optional<WatchlistItem> showAddDialog() {
        Dialog<WatchlistItem> dialog = new Dialog<>();
        dialog.setTitle("Add Symbol");
        dialog.setHeaderText("Add a new symbol to watchlist");
        dialog.initStyle(StageStyle.UTILITY);
        
        TextField symbolField = new TextField();
        symbolField.setPromptText("Symbol (e.g., BTC/USD)");
        symbolField.setPrefWidth(200);
        
        TextField companyField = new TextField();
        companyField.setPromptText("Company (e.g., Bitcoin)");
        companyField.setPrefWidth(200);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Symbol:"), 0, 0);
        grid.add(symbolField, 1, 0);
        grid.add(new Label("Company:"), 0, 1);
        grid.add(companyField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Validare
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        symbolField.textProperty().addListener((obs, oldVal, newVal) -> 
            addButton.setDisable(newVal.trim().isEmpty() || companyField.getText().trim().isEmpty())
        );
        companyField.textProperty().addListener((obs, oldVal, newVal) -> 
            addButton.setDisable(symbolField.getText().trim().isEmpty() || newVal.trim().isEmpty())
        );
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String symbol = symbolField.getText().trim().toUpperCase();
                String company = companyField.getText().trim();
                if (!symbol.isEmpty() && !company.isEmpty()) {
                    return new WatchlistItem(symbol, company);
                }
            }
            return null;
        });
        
        symbolField.requestFocus();
        return dialog.showAndWait();
    }
    
    /**
     * Dialog pentru confirmare ștergere
     */
    public static boolean showDeleteConfirmation(String symbol) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Symbol");
        alert.setHeaderText("Delete " + symbol + "?");
        alert.setContentText("Are you sure you want to remove this symbol from watchlist?");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Dialog pentru confirmare "Clear All"
     */
    public static boolean showClearAllConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All");
        alert.setHeaderText("Delete all symbols?");
        alert.setContentText("Are you sure you want to remove ALL symbols from the watchlist?");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Dialog pentru editare simbol
     */
    public static Optional<WatchlistItem> showEditDialog(WatchlistItem existingItem) {
        Dialog<WatchlistItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Symbol");
        dialog.setHeaderText("Edit symbol information");
        dialog.initStyle(StageStyle.UTILITY);
        
        TextField symbolField = new TextField(existingItem.getSymbol());
        TextField companyField = new TextField(existingItem.getCompany());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Symbol:"), 0, 0);
        grid.add(symbolField, 1, 0);
        grid.add(new Label("Company:"), 0, 1);
        grid.add(companyField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String symbol = symbolField.getText().trim().toUpperCase();
                String company = companyField.getText().trim();
                if (!symbol.isEmpty() && !company.isEmpty()) {
                    return new WatchlistItem(symbol, company);
                }
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
}