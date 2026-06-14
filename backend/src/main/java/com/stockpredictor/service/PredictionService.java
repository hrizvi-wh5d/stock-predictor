package com.stockpredictor.service;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * STOCK PREDICTION ENGINE - A-LEVEL MATHEMATICS
 * ================================================
 * This class implements three A-Level statistical techniques:
 *
 * 1. SIMPLE MOVING AVERAGE (SMA)
 *    - A-Level topic: Data processing / time series
 *    - Smooths out short-term fluctuations to show trend
 *    - SMA(n) = (P1 + P2 + ... + Pn) / n
 *
 * 2. LINEAR REGRESSION (on log prices)
 *    - A-Level topic: Statistics - Regression & Correlation
 *    - Fits a straight line y = a + bx to data points
 *    - We apply it to log(price) so the projection curves naturally
 *    - This is called log-linear regression
 *
 * 3. STANDARD DEVIATION & CONFIDENCE INTERVALS
 *    - A-Level topic: Statistics - Normal Distribution
 *    - σ (sigma) measures how spread out prices are
 *    - ±1σ captures ~68% of values, ±2σ captures ~95%
 */
@Service
public class PredictionService {

    /**
     * SIMPLE MOVING AVERAGE
     * Calculates average of last 'window' prices at each point
     * This smooths the jagged price line into a trend line
     *
     * Example: prices=[10,12,11,13,15], window=3
     * SMA at index 2 = (10+12+11)/3 = 11.0
     * SMA at index 3 = (12+11+13)/3 = 12.0
     */
    public List<Double> simpleMovingAverage(List<Double> prices, int window) {
        List<Double> sma = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            if (i < window - 1) {
                sma.add(null); // Not enough data yet for a full window
            } else {
                double sum = 0;
                for (int j = i - window + 1; j <= i; j++) {
                    sum += prices.get(j);
                }
                sma.add(sum / window); // The average of the window
            }
        }
        return sma;
    }

    /**
     * LINEAR REGRESSION ON LOG PRICES
     * A-Level Statistics: y = a + bx (line of best fit)
     *
     * Why log prices? Because stock prices multiply (not add).
     * A stock going from £10 to £20 is the same % change as £100 to £200.
     * Taking log() converts multiplicative growth to additive - making regression valid.
     *
     * Steps:
     * 1. Convert prices to log(price)
     * 2. Fit y = a + bx using least squares formulas:
     *    b = (n·Σxy - Σx·Σy) / (n·Σx² - (Σx)²)   ← gradient
     *    a = (Σy - b·Σx) / n                         ← y-intercept
     * 3. Predict future log(price), then convert back with e^y
     */
    public Map<String, Object> linearRegressionPredict(List<Double> prices, int futureDays) {
        int n = prices.size();
        double[] x = new double[n]; // x = day index (0, 1, 2, ...)
        double[] y = new double[n]; // y = log(price)

        for (int i = 0; i < n; i++) {
            x[i] = i;
            y[i] = Math.log(prices.get(i)); // Natural log
        }

        // Calculate sums needed for least squares formulas
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX  += x[i];
            sumY  += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        // Least squares gradient (b) and intercept (a)
        double b = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double a = (sumY - b * sumX) / n;

        // Calculate residuals to find standard deviation
        // Residual = actual y - predicted y (how far off the line each point is)
        double[] residuals = new double[n];
        double sumResidualsSq = 0;
        for (int i = 0; i < n; i++) {
            double predicted = a + b * x[i];
            residuals[i] = y[i] - predicted;
            sumResidualsSq += residuals[i] * residuals[i];
        }

        // Standard deviation of residuals (σ)
        // This measures typical scatter around the trend line
        // A-Level formula: σ = √(Σ(xi - x̄)² / n)
        double sigma = Math.sqrt(sumResidualsSq / n);

        // Generate predictions for future days
        List<Map<String, Double>> predictions = new ArrayList<>();
        for (int d = 1; d <= futureDays; d++) {
            int futureX = n - 1 + d;
            double logPredicted = a + b * futureX;

            // Convert back from log space to price
            double predicted   = Math.exp(logPredicted);
            double upper1sigma = Math.exp(logPredicted + sigma);       // +1σ (68% confidence)
            double lower1sigma = Math.exp(logPredicted - sigma);       // -1σ
            double upper2sigma = Math.exp(logPredicted + 2 * sigma);   // +2σ (95% confidence)
            double lower2sigma = Math.exp(logPredicted - 2 * sigma);   // -2σ

            Map<String, Double> point = new HashMap<>();
            point.put("day", (double) d);
            point.put("predicted", round(predicted));
            point.put("upper1", round(upper1sigma));
            point.put("lower1", round(lower1sigma));
            point.put("upper2", round(upper2sigma));
            point.put("lower2", round(lower2sigma));
            predictions.add(point);
        }

        // R² (R-squared) = how well the line fits the data
        // A-Level: correlation coefficient r, R² = r²
        // R²=1 means perfect fit, R²=0 means no linear relationship
        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            ssTot += (y[i] - meanY) * (y[i] - meanY);
            ssRes += residuals[i] * residuals[i];
        }
        double rSquared = 1 - (ssRes / ssTot);

        Map<String, Object> result = new HashMap<>();
        result.put("predictions", predictions);
        result.put("gradient", round(b));        // b: daily growth rate in log space
        result.put("intercept", round(a));       // a: starting log price
        result.put("rSquared", round(rSquared)); // fit quality
        result.put("sigma", round(sigma));       // volatility measure
        result.put("annualisedVolatility", round(sigma * Math.sqrt(252))); // 252 trading days/year
        return result;
    }

    /**
     * STANDARD DEVIATION of a price series
     * A-Level formula: σ = √( Σ(xi - x̄)² / n )
     * Used to describe how volatile (risky) a stock is
     */
    public double standardDeviation(List<Double> prices) {
        double mean = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = prices.stream()
                .mapToDouble(p -> (p - mean) * (p - mean))
                .average().orElse(0);
        return Math.sqrt(variance);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
