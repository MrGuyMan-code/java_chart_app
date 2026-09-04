package com.application.utils;

import java.util.List;

import com.application.models.WatchlistItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class WatchlistManager {
    
    // DOAR în memorie - gestionează lista
    private final ObservableList<WatchlistItem> data = FXCollections.observableArrayList();
    private final TableView<WatchlistItem> table;
    private final TableColumn<WatchlistItem, String> symbolColumn;
    private final TableColumn<WatchlistItem, String> companyColumn;
    
    public WatchlistManager(TableView<WatchlistItem> table, 
                           TableColumn<WatchlistItem, String> symbolColumn, 
                           TableColumn<WatchlistItem, String> companyColumn) {
        this.table = table;
        this.symbolColumn = symbolColumn;
        this.companyColumn = companyColumn;
        setupTable();
        
        // Watchlist-ul pornește GOL
        System.out.println("📭 Watchlist started empty. Use Add or Import to populate.");
    }
    
    private void setupTable() {
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        symbolColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        companyColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        
        table.setItems(data);
        
        // Placeholder când lista e goală
        table.setPlaceholder(new javafx.scene.control.Label(
            "📭 No items in watchlist.\n\n" +
            "• Click 'Add' to add a symbol manually\n" +
            "• Click 'Import' to load from a CSV file"
        ));
    }
    
    // ===== OPERAȚII CRUD (DOAR ÎN MEMORIE) =====
    
    public void addItem(WatchlistItem item) {
        data.add(item);
        System.out.println("✅ Added: " + item.getSymbol() + " (" + data.size() + " items in memory)");
    }
    
    public void updateItem(WatchlistItem oldItem, WatchlistItem newItem) {
        int index = data.indexOf(oldItem);
        if (index >= 0) {
            data.set(index, newItem);
            System.out.println("✅ Updated: " + oldItem.getSymbol() + " -> " + newItem.getSymbol());
        }
    }
    
    public void removeItem(WatchlistItem item) {
        data.remove(item);
        System.out.println("✅ Removed: " + item.getSymbol() + " (" + data.size() + " items in memory)");
    }
    
    public void removeSelected() {
        WatchlistItem selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            removeItem(selected);
        }
    }
    
    public void clearAll() {
        data.clear();
        System.out.println("✅ Cleared all items from memory");
    }
    
    public void setItems(List<WatchlistItem> items) {
        data.clear();
        data.addAll(items);
        System.out.println("✅ Set " + items.size() + " items in memory");
    }
    
    public void addItems(List<WatchlistItem> items) {
        data.addAll(items);
        System.out.println("✅ Added " + items.size() + " items. Total: " + data.size());
    }
    
    // ===== GETTERS =====
    
    public ObservableList<WatchlistItem> getData() {
        return data;
    }
    
    public WatchlistItem getSelected() {
        return table.getSelectionModel().getSelectedItem();
    }
    
    public int getItemCount() {
        return data.size();
    }
    
    public boolean isEmpty() {
        return data.isEmpty();
    }
}