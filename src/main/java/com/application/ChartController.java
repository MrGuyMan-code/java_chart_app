package com.application;

import com.application.utils.ChartUtils;
import com.application.handlers.MouseHandlers;
import com.application.models.WatchlistItem;
import com.application.utils.WatchlistManager;
import com.application.utils.WatchlistStorage;
import com.application.utils.WatchlistStorage.ImportResult;
import com.application.services.WatchlistService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.OHLCDataset;

import java.net.URL;
import java.util.ResourceBundle;

public class ChartController implements Initializable{

    @FXML
    private StackPane chartContainer;
    @FXML 
    private Button cursorButton;  // Pan mode
    @FXML 
    private Button zoomButton;    // Zoom mode (default)
    
    @FXML
    private TableView<WatchlistItem> watchlistTable;
    @FXML
    private TableColumn<WatchlistItem, String> symbolColumn;
    @FXML
    private TableColumn<WatchlistItem, String> companyColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button addToWatchlistButton;
    @FXML
    private Button searchButton;
    @FXML
    private TextField searchTextField;
    

    private JFreeChart chart;
    
    private ChartViewer viewer;
    
    private MouseHandlers mouseHandlers;
    
    private WatchlistManager watchlistManager;
    
    private WatchlistService watchlistService;
    
    private OHLCDataset dataset;
    
    private static final int YEARS = 10;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
        // Creează date de test
        //OHLCDataset dataset = ChartUtils.createSampleDataset(30, 50000.0, 1000.0);
        String symbol = "AAPL";
		dataset = ChartUtils.fetchChartData(symbol, YEARS);
		
		ChartUtils.debugWeekendData(dataset);
		
		searchTextField.setText(symbol);
		
        // Creează graficul
        chart = ChartUtils.createCandlestickChart(symbol, dataset);

        // Aplică tema întunecată
        ChartUtils.applyDarkTheme(chart);

        // Afișează graficul
        viewer = new ChartViewer(chart);
        
        
        // 🔥 ADUAGĂ ACEASTĂ LINIE - activează zoom-ul cu mouse-ul
        ChartUtils.setupMouseZoom(chart, viewer, dataset);
        
        mouseHandlers = new MouseHandlers(chart, viewer, zoomButton, cursorButton);
        
        mouseHandlers.setDataset(dataset);
        
        // Setup watchlist
        watchlistManager = new WatchlistManager(watchlistTable, symbolColumn, companyColumn);
        
        // 🔥🔥🔥 INITIALIZEAZĂ SERVICIUL AICI 🔥🔥🔥
        watchlistService = new WatchlistService(watchlistManager);
        
        watchlistTable.setOnMouseClicked(event -> {
            // Check if double-click (optional - can use single click)
            if (event.getClickCount() == 1) {
                WatchlistItem selectedItem = watchlistTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // 🔥 SET THE SYMBOL IN THE SEARCH FIELD
                    searchTextField.setText(selectedItem.getSymbol());
                    
                    searchButton.fire();
                    // 🔥 AUTOMATICALLY SEARCH (optional)
                    // searchButtonPressed(null);
                    
                    System.out.println("🔍 Selected symbol: " + selectedItem.getSymbol());
                }
            }
        });
        
        chartContainer.getChildren().add(viewer);

		
	}
	
	public void searchButtonPressed(ActionEvent event) {
		String symbol = searchTextField.getText();
		dataset = ChartUtils.fetchChartData(symbol, YEARS);
		chart = ChartUtils.createCandlestickChart(symbol, dataset);
        // Aplică tema întunecată
        ChartUtils.applyDarkTheme(chart);

        // Afișează graficul
        viewer = new ChartViewer(chart);
        
        
        // 🔥 ADUAGĂ ACEASTĂ LINIE - activează zoom-ul cu mouse-ul
        ChartUtils.setupMouseZoom(chart, viewer, dataset);
        
        mouseHandlers = new MouseHandlers(chart, viewer, zoomButton, cursorButton);
        
        mouseHandlers.setDataset(dataset);
        
        chartContainer.getChildren().add(viewer);
		
	}
	
    public void addToWatchlist(ActionEvent event) {
        // 🔥 O SINGURĂ LINIE în controller pentru buton!
        watchlistService.addSymbol();
    }
	
	public void cursorButtonPressed(ActionEvent event) {
		mouseHandlers.setPanMode();
	}
	
	public void zoomButtonPressed(ActionEvent event) {
		mouseHandlers.setZoomMode();
	}
	
    
    @FXML
    private void handleImport() {
        Stage stage = (Stage) watchlistTable.getScene().getWindow();
        ImportResult result = WatchlistStorage.importWithConflictHandling(
            watchlistManager.getData(), stage
        );
        if (result.isCanceled()) return;
        // Aplică rezultatul
        if (result.isReplace()) watchlistManager.setItems(result.getItems());
        else if (result.isAdd()) watchlistManager.addItems(result.getItems());
    }
    
    @FXML
    private void handleExport() {
        Stage stage = (Stage) watchlistTable.getScene().getWindow();
        // Storage se ocupă de tot: FileChooser, salvare, mesaje
        WatchlistStorage.exportToFile(watchlistManager.getData(), stage);
    }

}
