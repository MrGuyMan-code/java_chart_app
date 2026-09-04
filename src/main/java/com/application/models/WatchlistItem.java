package com.application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WatchlistItem {
    
    private StringProperty symbol;
    private StringProperty company;
    
    public WatchlistItem(String symbol, String company) {
        this.symbol = new SimpleStringProperty(symbol);
        this.company = new SimpleStringProperty(company);
    }
    
    public String getSymbol() {
        return symbol.get();
    }
    
    public StringProperty symbolProperty() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol.set(symbol);
    }
    
    public String getCompany() {
        return company.get();
    }
    
    public StringProperty companyProperty() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company.set(company);
    }
    
    @Override
    public String toString() {
        return symbol.get() + " - " + company.get();
    }
}