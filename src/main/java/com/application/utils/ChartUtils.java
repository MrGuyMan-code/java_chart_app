package com.application.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;        // ← IMPORT CORECT
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;

import com.application.services.YahooFinanceService;

import javafx.scene.input.ScrollEvent;



import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChartUtils {
	
	public static final double CANDLE_WIDTH_RATIO = 0.95;
	
	public static JFreeChart createCandlestickChart(String title, OHLCDataset dataset) {
	    JFreeChart chart = ChartFactory.createCandlestickChart(
	            title,
	            "Time",
	            "Price (USD)",
	            dataset,
	            false
	    );

	    XYPlot plot = (XYPlot) chart.getPlot();

	    // 🔥 CONFIGUREAZĂ AXELE
	    DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
	    domainAxis.setAutoRange(true);
	    domainAxis.setFixedAutoRange(90L * 24 * 60 * 60 * 1000);

	    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	    rangeAxis.setAutoRange(true);
	    rangeAxis.setAutoRangeIncludesZero(false);

	    // 🔥 CREEZĂ RENDERER-UL PERSONALIZAT PENTRU CANDLESTICK
	    CandlestickRenderer renderer = new CandlestickRenderer() {
	        @Override
	        public Paint getItemPaint(int series, int item) {
	            OHLCDataset highLowData = (OHLCDataset) getPlot().getDataset(series);
	            if (highLowData == null) {
	                return Color.GRAY;
	            }
	            double yOpen = highLowData.getOpenValue(series, item);
	            double yClose = highLowData.getCloseValue(series, item);
	            return (yClose >= yOpen) ? Color.GREEN : Color.RED;
	        }

	        @Override
	        public Paint getItemOutlinePaint(int series, int item) {
	            return getItemPaint(series, item);
	        }
	    };

	    renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
	    renderer.setAutoWidthFactor(CANDLE_WIDTH_RATIO);

	    // 🔥 SETEAZĂ RENDERER-UL LA INDEXUL 0
	    plot.setRenderer(0, renderer);
	    plot.setDataset(0, dataset);

	    return chart;
	}
   
    
    public static OHLCDataset createSampleDataset(int count, double startPrice, double volatility) {
        long now = System.currentTimeMillis();
        OHLCDataItem[] items = new OHLCDataItem[count];

        double price = startPrice;
        for (int i = 0; i < count; i++) {
            double open = price + (Math.random() - 0.5) * volatility;
            double close = open + (Math.random() - 0.5) * volatility * 0.8;
            double high = Math.max(open, close) + Math.random() * volatility * 0.5;
            double low = Math.min(open, close) - Math.random() * volatility * 0.5;
            long time = now - (count - 1 - i) * 3600000L;

            items[i] = new OHLCDataItem(
                    new Date(time),
                    Math.round(open * 100.0) / 100.0,
                    Math.round(high * 100.0) / 100.0,
                    Math.round(low * 100.0) / 100.0,
                    Math.round(close * 100.0) / 100.0,
                    1000
            );
            price = close;
        }

        return new DefaultOHLCDataset("Sample", items);
    }

    public static OHLCDataset fetchChartData(String symbol) {
        try {
            // YahooFinanceService.fetchFullHistory() returnează List<OHLCDataItem>
        	OHLCDataset dataset = YahooFinanceService.fetchFullHistory(symbol);
            
            if (dataset != null && dataset.getItemCount(0) > 0) {
                System.out.println("✅ Loaded " + dataset.getItemCount(0) + " candles for " + symbol + " from Yahoo");
                return dataset;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Yahoo fetch failed: " + e.getMessage());
        }
        
        // Fallback la date de test
        //System.out.println("⚠️ Using sample data for " + symbol);
        //return createSampleDataset(30, 50000.0, 1000.0);
        return null;
    }
    
    /**
     * 🔥 VERSIUNE CU ANI (opțional)
     */
    public static OHLCDataset fetchChartData(String symbol, int years) {
        try {
            // YahooFinanceService.fetchFullHistory() returnează List<OHLCDataItem>
        	OHLCDataset dataset = YahooFinanceService.fetchHistory(symbol, years);
            
            if (dataset != null && dataset.getItemCount(0) > 0) {
                System.out.println("✅ Loaded " + dataset.getItemCount(0) + " candles for " + symbol + " from Yahoo");
                return dataset;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Yahoo fetch failed: " + e.getMessage());
        }
        
        // Fallback la date de test
        System.out.println("⚠️ Using sample data for " + symbol);
        return createSampleDataset(30, 50000.0, 1000.0);
    }
    
    public static void applyDarkTheme(JFreeChart chart) {
        chart.setBackgroundPaint(Color.decode("#1a1a2e"));
        chart.getTitle().setPaint(Color.WHITE);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.decode("#16213e"));
        plot.setDomainGridlinePaint(Color.decode("#2c3e50"));
        plot.setRangeGridlinePaint(Color.decode("#2c3e50"));

        // DateAxis pentru axa X
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setLabelPaint(Color.WHITE);
        domainAxis.setTickLabelPaint(Color.WHITE);

        // NumberAxis pentru axa Y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelPaint(Color.WHITE);
        rangeAxis.setTickLabelPaint(Color.WHITE);
    }
    
    /*
    public static void setupMouseZoom(JFreeChart chart, ChartViewer viewer) {
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        refreshCandleWidth(chart);

        viewer.addEventFilter(ScrollEvent.SCROLL, event -> {
        	double mouseX = event.getX();
            // 70px = lățimea aproximativă a zonei cu numere de preț
            boolean onYAxis = mouseX < 70;
            // Factorul de zoom - poți ajusta după preferințe
            double zoomFactor = event.getDeltaY() > 0 ? 0.93 : 1.025;
            
            if (onYAxis) {
	            // Calculează noul interval pentru axa Y (preț)
	            double lower = rangeAxis.getLowerBound();
	            double upper = rangeAxis.getUpperBound();
	            double center = (lower + upper) / 2;
	            double range = (upper - lower) * zoomFactor / 2;
	            
	            rangeAxis.setRange(center - range, center + range);
	            // Recalculează lățimea lumânărilor
	            refreshCandleWidth(chart);
            }else {
                // 🔥 ZOOM PE AXA X (TIMP) - când e în restul graficului
                long lowerX = (long) domainAxis.getLowerBound();
                long upperX = (long) domainAxis.getUpperBound();
                long centerX = (lowerX + upperX) / 2;
                long rangeX = (long) ((upperX - lowerX) * zoomFactor / 2);
                
                long minRangeX = 3600000; // 1 oră
                if (rangeX >= minRangeX / 2) {
                    domainAxis.setRange(centerX - rangeX, upperX);
                    
                    // Recalculează lățimea lumânărilor
                    refreshCandleWidth(chart);
                }
            }
            event.consume(); // Previne scroll-ul paginii
        });
    }*/
    
    public static void setupMouseZoom(JFreeChart chart, ChartViewer viewer, OHLCDataset fullDataset) {
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        refreshCandleWidth(chart);

        viewer.addEventFilter(ScrollEvent.SCROLL, event -> {
            double mouseX = event.getX();
            boolean onYAxis = mouseX < 70;
            double zoomFactor = event.getDeltaY() > 0 ? 0.93 : 1.025;
            
            if (onYAxis) {
                double lower = rangeAxis.getLowerBound();
                double upper = rangeAxis.getUpperBound();
                double center = (lower + upper) / 2;
                double range = (upper - lower) * zoomFactor / 2;
                
                rangeAxis.setRange(center - range, center + range);
                refreshCandleWidth(chart);
                
            } else {
                // 🔥 ZOOM PE AXA X (TIMP)
                long lowerX = (long) domainAxis.getLowerBound();
                long upperX = (long) domainAxis.getUpperBound();
                long centerX = (lowerX + upperX) / 2;
                long rangeX = (long) ((upperX - lowerX) * zoomFactor / 2);
                
                long minRangeX = 3600000; // 1 oră
                if (rangeX >= minRangeX / 2) {
                    domainAxis.setRange(centerX - rangeX, upperX);
                    
                    // 🔥 EXTRACT SUBSET PENTRU NOUL INTERVAL
                    double newLower = domainAxis.getLowerBound();
                    double newUpper = domainAxis.getUpperBound();
                    OHLCDataset subset = ChartUtils.extractSubsetByRange(fullDataset, newLower, newUpper);
                    
                    if (subset != null && subset.getItemCount(0) > 0) {
                        // 🔥 ACTUALIZEAZĂ GRAFICUL CU SUBSET-UL
                        ChartUtils.updateChartWithNewData(chart, subset);
                        // Restaurează intervalul
                        domainAxis.setRange(newLower, newUpper);
                    }
                }
            }
            event.consume();
        });
    }

    private static void refreshCandleWidth(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        
        if (renderer != null) {
            renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
            renderer.setAutoWidthFactor(CANDLE_WIDTH_RATIO);
        }
    }

    
    /**
     * 🔥 EXTRACT SUBSET DIN DATASET PE BAZA LIMITELOR
     */
    public static OHLCDataset extractSubsetByRange(OHLCDataset fullDataset, double xLower, double xUpper) {
        if (fullDataset == null || fullDataset.getItemCount(0) == 0) {
            return fullDataset;
        }
        
        int totalItems = fullDataset.getItemCount(0);
        
        // Găsește primul index >= xLower
        int startIndex = 0;
        for (int i = 0; i < totalItems; i++) {
            // Folosește getX() pentru a obține timestamp-ul
            double timestamp = fullDataset.getXValue(0, i);
            if (timestamp >= xLower) {
                startIndex = i;
                break;
            }
        }
        
        // Găsește ultimul index <= xUpper
        int endIndex = totalItems - 1;
        for (int i = totalItems - 1; i >= 0; i--) {
            double timestamp = fullDataset.getXValue(0, i);
            if (timestamp <= xUpper) {
                endIndex = i;
                break;
            }
        }
        
        int count = endIndex - startIndex + 1;
        if (count <= 0 || count == totalItems) {
            return fullDataset;
        }
        
        // Construiește noul array de OHLCDataItem
        OHLCDataItem[] subset = new OHLCDataItem[count];
        for (int i = 0; i < count; i++) {
            int index = startIndex + i;
            Date date = new Date((long) fullDataset.getXValue(0, index));
            double open = fullDataset.getOpenValue(0, index);
            double high = fullDataset.getHighValue(0, index);
            double low = fullDataset.getLowValue(0, index);
            double close = fullDataset.getCloseValue(0, index);
            long volume = (long) fullDataset.getVolumeValue(0, index);
            
            subset[i] = new OHLCDataItem(date, open, high, low, close, volume);
        }
        
        //System.out.println("📊 Extracted " + count + " candles between " + 
        //                   new Date((long) xLower) + " and " + new Date((long) xUpper));
        return new DefaultOHLCDataset("Subset", subset);
    }
    /**
     * 🔥 ACTUALIZEAZĂ GRAFICUL CU UN NOU DATASET
     */
    public static void updateChartWithNewData(JFreeChart chart, OHLCDataset newDataset) {
        XYPlot plot = (XYPlot) chart.getPlot();

        // 🔥 SETEAZĂ NOUL DATASET LA INDEXUL 0
        plot.setDataset(0, newDataset);

        // 🔥 RECONFIGUREAZĂ RENDERER-UL EXISTENT
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer(0);
        if (renderer != null) {
            renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
            renderer.setAutoWidthFactor(CANDLE_WIDTH_RATIO);
        }

        chart.fireChartChanged();
    }
    
    public static void debugWeekendData(OHLCDataset dataset) {
        if (dataset == null || dataset.getItemCount(0) == 0) {
            System.out.println("⚠️ Dataset is empty");
            return;
        }
        
        int total = dataset.getItemCount(0);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy");
        
        System.out.println("📊 Debug: First 20 dates with day of week:");
        int count = Math.min(20, total);
        for (int i = 0; i < count; i++) {
            Date date = new Date((long) dataset.getXValue(0, i));
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String dayName = "";
            switch (dayOfWeek) {
                case Calendar.MONDAY:    dayName = "MON"; break;
                case Calendar.TUESDAY:   dayName = "TUE"; break;
                case Calendar.WEDNESDAY: dayName = "WED"; break;
                case Calendar.THURSDAY:  dayName = "THU"; break;
                case Calendar.FRIDAY:    dayName = "FRI"; break;
                case Calendar.SATURDAY:  dayName = "SAT ⚠️"; break;
                case Calendar.SUNDAY:    dayName = "SUN ⚠️"; break;
            }
            System.out.println("  " + sdf.format(date) + " → " + dayName);
        }
        
        // Numără câte weekend-uri sunt
        int weekendCount = 0;
        for (int i = 0; i < total; i++) {
            Date date = new Date((long) dataset.getXValue(0, i));
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendCount++;
            }
        }
        System.out.println("📊 Total weekend days: " + weekendCount + " out of " + total + " candles");
    }
}