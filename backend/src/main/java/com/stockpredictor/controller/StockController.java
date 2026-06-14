package com.stockpredictor.controller;

import com.stockpredictor.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private PredictionService predictionService;

    private static final Map<String, String[]> STOCK_LISTS = new HashMap<>();
    static {
        STOCK_LISTS.put("NASDAQ", new String[]{
            "AAPL:Apple Inc","MSFT:Microsoft Corporation","GOOGL:Alphabet (Google)",
            "AMZN:Amazon.com","NVDA:NVIDIA Corporation","TSLA:Tesla Inc",
            "META:Meta Platforms","NFLX:Netflix Inc","INTC:Intel Corporation","AMD:Advanced Micro Devices"
        });
        STOCK_LISTS.put("FTSE", new String[]{
            "SHEL.L:Shell PLC","AZN.L:AstraZeneca","HSBA.L:HSBC Holdings",
            "ULVR.L:Unilever","BP.L:BP PLC","GSK.L:GSK PLC",
            "RIO.L:Rio Tinto","VOD.L:Vodafone Group","LLOY.L:Lloyds Banking","BARC.L:Barclays PLC"
        });
    }

    @GetMapping("/list")
    public ResponseEntity<?> getStockList(@RequestParam String market) {
        String[] stocks = STOCK_LISTS.getOrDefault(market.toUpperCase(), new String[]{});
        List<Map<String, String>> result = new ArrayList<>();
        for (String s : stocks) {
            String[] parts = s.split(":");
            Map<String, String> item = new HashMap<>();
            item.put("symbol", parts[0]);
            item.put("name", parts[1]);
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam String symbol,
                                        @RequestParam(defaultValue = "1y") String period) {
        try {
            long endTime = System.currentTimeMillis() / 1000;
            long startTime = switch (period) {
                case "3m" -> endTime - (90L * 24 * 3600);
                case "6m" -> endTime - (180L * 24 * 3600);
                case "2y" -> endTime - (730L * 24 * 3600);
                default   -> endTime - (365L * 24 * 3600);
            };

            String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d",
                symbol, startTime, endTime
            );

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("User-Agent", "Mozilla/5.0");
                request.getHeaders().set("Accept", "application/json");
                return execution.execute(request, body);
            });

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> chart = (Map<String, Object>) response.get("chart");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");

            if (results == null || results.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No data found for: " + symbol));
            }

            Map<String, Object> data = results.get(0);

            // FIX: Use Number instead of Long to handle both Integer and Long from Yahoo Finance
            @SuppressWarnings("unchecked")
            List<Number> timestamps = (List<Number>) data.get("timestamp");

            @SuppressWarnings("unchecked")
            Map<String, Object> indicators = (Map<String, Object>) data.get("indicators");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) indicators.get("quote");
            @SuppressWarnings("unchecked")
            List<Number> closes = (List<Number>) quotes.get(0).get("close");

            List<Map<String, Object>> priceHistory = new ArrayList<>();
            List<Double> validPrices = new ArrayList<>();

            for (int i = 0; i < timestamps.size(); i++) {
                if (closes.get(i) != null) {
                    // Use .longValue() to safely convert Number to long
                    long ts = timestamps.get(i).longValue();
                    String date = LocalDate.ofEpochDay(ts / 86400).toString();
                    double price = Math.round(closes.get(i).doubleValue() * 100.0) / 100.0;
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", date);
                    point.put("price", price);
                    priceHistory.add(point);
                    validPrices.add(price);
                }
            }

            double currentPrice = validPrices.get(validPrices.size() - 1);
            double firstPrice = validPrices.get(0);
            double changePercent = ((currentPrice - firstPrice) / firstPrice) * 100;
            double stdDev = predictionService.standardDeviation(validPrices);

            return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "history", priceHistory,
                "currentPrice", Math.round(currentPrice * 100.0) / 100.0,
                "changePercent", Math.round(changePercent * 100.0) / 100.0,
                "standardDeviation", Math.round(stdDev * 100.0) / 100.0,
                "dataPoints", validPrices.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch data: " + e.getMessage()));
        }
    }

    @GetMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam String symbol,
                                     @RequestParam(defaultValue = "30") int futureDays,
                                     @RequestParam(defaultValue = "20") int smaWindow) {
        try {
            ResponseEntity<?> historyResponse = getHistory(symbol, "1y");
            if (!historyResponse.getStatusCode().is2xxSuccessful()) {
                return historyResponse;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> histData = (Map<String, Object>) historyResponse.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> history = (List<Map<String, Object>>) histData.get("history");

            List<Double> prices = new ArrayList<>();
            for (Map<String, Object> point : history) {
                prices.add(((Number) point.get("price")).doubleValue());
            }

            List<Double> sma = predictionService.simpleMovingAverage(prices, smaWindow);
            Map<String, Object> regressionResult = predictionService.linearRegressionPredict(prices, futureDays);

            return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "history", history,
                "sma", sma,
                "smaWindow", smaWindow,
                "futureDays", futureDays,
                "regression", regressionResult,
                "currentPrice", histData.get("currentPrice"),
                "standardDeviation", histData.get("standardDeviation")
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
