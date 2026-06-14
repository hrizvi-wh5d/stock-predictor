package com.stockpredictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication tells Spring this is where the app starts
// It also auto-scans all files in this package and sub-packages
@SpringBootApplication
public class StockPredictorApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockPredictorApplication.class, args);
        System.out.println("✅ Stock Predictor Backend is running on http://localhost:8080");
        System.out.println("📊 H2 Database Console: http://localhost:8080/h2-console");
    }
}
