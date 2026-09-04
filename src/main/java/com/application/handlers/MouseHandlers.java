package com.application.handlers;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.OHLCDataset;

import com.application.utils.ChartUtils;


// the mouse handlers
public class MouseHandlers {
    
    private final JFreeChart chart;
    private final ChartViewer viewer;
    private final Button zoomButton;
    private final Button cursorButton;
    
    private boolean isPanMode = true;
    
    // Pan variables
    private double panStartX, panStartY;
    private double panEndX, panEndY;
    private double xLower, xUpper, yLower, yUpper;
    
    // Zoom variables
    private double zoomStartX, zoomStartY;
    
    //data
    private OHLCDataset dataset;
    
    public MouseHandlers(JFreeChart chart, ChartViewer viewer, Button zoomButton, Button cursorButton) {
        this.chart = chart;
        this.viewer = viewer;
        this.zoomButton = zoomButton;
        this.cursorButton = cursorButton;
        setupHandlers();
        setPanMode(); // Default
    }
    
    public void setDataset(OHLCDataset newDataset)
    {
    	this.dataset = newDataset;  
    }
    
    private void setupHandlers() {
        viewer.setOnMousePressed(this::handleMousePressed);
        viewer.setOnMouseDragged(this::handleMouseDragged);
        viewer.setOnMouseReleased(this::handleMouseReleased);
       
    }
    
    private void handleMousePressed(MouseEvent e) {
        if (isPanMode) {
            panStartX = e.getX();
            panStartY = e.getY();
            
            XYPlot plot = chart.getXYPlot();
            xLower = plot.getDomainAxis().getLowerBound();
            xUpper = plot.getDomainAxis().getUpperBound();
            yLower = plot.getRangeAxis().getLowerBound();
            yUpper = plot.getRangeAxis().getUpperBound();
            
            viewer.setCursor(Cursor.CLOSED_HAND);
            e.consume();
        } else {
            zoomStartX = e.getX();
            zoomStartY = e.getY();
        }
    }
    
    private void handleMouseDragged(MouseEvent e) {
        if (isPanMode) {
            // Calculează diferența curentă
            double currentX = e.getX();
            double currentY = e.getY();
            double deltaX = currentX - panStartX;
            double deltaY = currentY - panStartY;
            
            // Aplică pan-ul în timp real
            applyPan(deltaX, deltaY);

            e.consume();
        } else {
            // Zoom drag
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (isPanMode) {
            // Calculează diferența finală
            double currentX = e.getX();
            double currentY = e.getY();
            double deltaX = currentX - panStartX;
            double deltaY = currentY - panStartY;
            
            // Dacă e doar click (nu drag), ignoră
            if (Math.abs(deltaX) > 2 || Math.abs(deltaY) > 2) {
                applyPan(deltaX, deltaY);
            }
            
            viewer.setCursor(Cursor.OPEN_HAND);
            e.consume();
        }
    }
 
    private void applyPan(double deltaX, double deltaY) {
    	 XYPlot plot = chart.getXYPlot();
         
         double width = viewer.getWidth();
         double height = viewer.getHeight();
         
         viewer.hideZoomRectangle();
         
         // Convertește pixelii în valori pe axe
         double axisDeltaX = (xUpper - xLower) * (deltaX / width);
         double axisDeltaY = (yUpper - yLower) * (deltaY / height);
         
         // Aplică noile limite
         plot.getDomainAxis().setLowerBound(xLower - axisDeltaX);
         plot.getDomainAxis().setUpperBound(xUpper - axisDeltaX);
         plot.getRangeAxis().setLowerBound(yLower + axisDeltaY);  // Menținem - pentru că axa Y e inversată
         plot.getRangeAxis().setUpperBound(yUpper + axisDeltaY);
         
         refreshChartWithVisibleRange();
    }
    
    private void refreshChartWithVisibleRange() {
        if (dataset == null) {
            System.out.println("⚠️ Dataset is null, cannot refresh");
            return;
        }
        
        XYPlot plot = chart.getXYPlot();
        
        // Obține limitele curente ale axei X
        double currentXLower = plot.getDomainAxis().getLowerBound();
        double currentXUpper = plot.getDomainAxis().getUpperBound();
        
        // Extrage subsetul pentru intervalul vizibil
        OHLCDataset subset = ChartUtils.extractSubsetByRange(dataset, currentXLower, currentXUpper);
        
        if (subset != null && subset.getItemCount(0) > 0) {
            // Actualizează graficul cu noul subset
            ChartUtils.updateChartWithNewData(chart, subset);
            
            // Restaurează intervalul vizibil (important!)
            plot.getDomainAxis().setRange(currentXLower, currentXUpper);
            
            //System.out.println("🔄 Chart updated with " + subset.getItemCount(0) + " candles");
        }
    }
    
    // Metode publice pentru schimbarea modului
    public void setZoomMode() {
        isPanMode = false;
        viewer.setCursor(Cursor.DEFAULT);
        System.out.println("🔍 Zoom Mode");
    }
    
    public void setPanMode() {
        isPanMode = true;
        viewer.setCursor(Cursor.OPEN_HAND);
        //viewer.hideZoomRectangle();
        System.out.println("✚ Pan Mode");
    }
    
    public boolean isPanMode() {
        return isPanMode;
    }
}