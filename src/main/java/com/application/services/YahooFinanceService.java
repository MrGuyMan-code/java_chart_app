package com.application.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class YahooFinanceService {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetch full history for a symbol from Yahoo Finance
     */
    public static OHLCDataset fetchFullHistory(String symbol) {
        try {
            System.out.println("📥 Fetching full history for " + symbol + " (max)");
            
            // Yahoo Finance v8 API - folosește "max" pentru istoric complet
            String urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + 
                    "?interval=1d&range=max&includePrePost=false";
            
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ HTTP Error: " + responseCode);
                return null;
            }
            
            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            conn.disconnect();
            
            // Parsează JSON-ul
            JsonNode root = mapper.readTree(response);
            JsonNode result = root.get("chart").get("result");
            
            if (result == null || result.isEmpty()) {
                System.err.println("❌ No data found for " + symbol);
                return null;
            }
            
            JsonNode data = result.get(0);
            JsonNode timestamps = data.get("timestamp");
            JsonNode quote = data.get("indicators").get("quote").get(0);
            
            List<OHLCDataItem> items = new ArrayList<>();
            
            for (int i = 0; i < timestamps.size(); i++) {
                long timestamp = timestamps.get(i).asLong() * 1000;
                
                JsonNode openNode = quote.get("open").get(i);
                JsonNode highNode = quote.get("high").get(i);
                JsonNode lowNode = quote.get("low").get(i);
                JsonNode closeNode = quote.get("close").get(i);
                JsonNode volumeNode = quote.get("volume").get(i);
                
                // Verifică dacă datele sunt valide (nu null)
                if (openNode == null || openNode.isNull() ||
                    highNode == null || highNode.isNull() ||
                    lowNode == null || lowNode.isNull() ||
                    closeNode == null || closeNode.isNull() ||
                    volumeNode == null || volumeNode.isNull()) {
                    continue;
                }
                
                double open = openNode.asDouble();
                double high = highNode.asDouble();
                double low = lowNode.asDouble();
                double close = closeNode.asDouble();
                long volume = volumeNode.asLong();
                
                if (open > 0 && high > 0 && low > 0 && close > 0) {
                    items.add(new OHLCDataItem(
                        new Date(timestamp), 
                        open, high, low, close, 
                        volume
                    ));
                }
            }
            
            if (items.isEmpty()) {
                System.err.println("❌ No valid data found for " + symbol);
                return null;
            }
            
            OHLCDataItem[] array = items.toArray(new OHLCDataItem[0]);
            System.out.println("✅ Fetched " + array.length + " candles for " + symbol);
            
            return new DefaultOHLCDataset(symbol, array);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching data for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Fetch history for a specific number of years
     */
    public static OHLCDataset fetchHistory(String symbol, int years) {
        try {
            System.out.println("📥 Fetching " + years + " years of history for " + symbol);
            
            String urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + 
                            "?interval=1d&range=" + years + "y" + "&includePrePost=false";
            
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ HTTP Error: " + responseCode);
                return null;
            }
            
            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();
            conn.disconnect();
            
            JsonNode root = mapper.readTree(response);
            JsonNode result = root.get("chart").get("result");
            
            if (result == null || result.isEmpty()) {
                return null;
            }
            
            JsonNode data = result.get(0);
            JsonNode timestamps = data.get("timestamp");
            JsonNode quote = data.get("indicators").get("quote").get(0);
            
            List<OHLCDataItem> items = new ArrayList<>();
            
            for (int i = 0; i < timestamps.size(); i++) {
                long timestamp = timestamps.get(i).asLong() * 1000;
                
                JsonNode openNode = quote.get("open").get(i);
                JsonNode highNode = quote.get("high").get(i);
                JsonNode lowNode = quote.get("low").get(i);
                JsonNode closeNode = quote.get("close").get(i);
                JsonNode volumeNode = quote.get("volume").get(i);
                
                if (openNode == null || openNode.isNull() ||
                    highNode == null || highNode.isNull() ||
                    lowNode == null || lowNode.isNull() ||
                    closeNode == null || closeNode.isNull() ||
                    volumeNode == null || volumeNode.isNull()) {
                    continue;
                }
                
                double open = openNode.asDouble();
                double high = highNode.asDouble();
                double low = lowNode.asDouble();
                double close = closeNode.asDouble();
                long volume = volumeNode.asLong();
                
                if (open > 0 && high > 0 && low > 0 && close > 0) {
                    items.add(new OHLCDataItem(
                        new Date(timestamp), 
                        open, high, low, close, 
                        volume
                    ));
                }
            }
            
            if (items.isEmpty()) {
                return null;
            }
            
            OHLCDataItem[] array = items.toArray(new OHLCDataItem[0]);
            System.out.println("✅ Fetched " + array.length + " candles for " + symbol);
            
            return new DefaultOHLCDataset(symbol, array);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching data for " + symbol + ": " + e.getMessage());
            return null;
        }
    }
}