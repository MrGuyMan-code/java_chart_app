package com.application.services;

import org.jfree.data.xy.OHLCDataset;

public interface DataService {
    OHLCDataset fetchFullHistory(String symbol);
    OHLCDataset fetchHistory(String symbol, int years);
    boolean isAvailable();
}
