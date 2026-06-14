import React, { useMemo } from 'react';
import {
  ComposedChart, Line, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, ReferenceLine
} from 'recharts';

export default function StockChart({ data, futureDays }) {
  // Merge historical data + prediction data into one chart dataset
  const chartData = useMemo(() => {
    if (!data) return [];

    const { history, sma, regression } = data;
    const predictions = regression?.predictions || [];

    // Historical section
    const historicalPoints = history.map((point, i) => ({
      date: point.date,
      price: point.price,
      sma: sma[i] ?? null,
      type: 'historical'
    }));

    // Only show last 90 days of history to keep chart readable
    const recentHistory = historicalPoints.slice(-90);

    // Prediction section — starts from where history ends
    const lastHistDate = new Date(history[history.length - 1].date);
    const predictionPoints = predictions.map((p, i) => {
      const d = new Date(lastHistDate);
      d.setDate(d.getDate() + i + 1);
      // Skip weekends (markets closed)
      while (d.getDay() === 0 || d.getDay() === 6) d.setDate(d.getDate() + 1);
      return {
        date: d.toISOString().split('T')[0],
        predicted: p.predicted,
        upper1: p.upper1,
        lower1: p.lower1,
        upper2: p.upper2,
        lower2: p.lower2,
        type: 'prediction'
      };
    });

    // Join: last historical point bridges into predictions
    const bridgePoint = {
      ...recentHistory[recentHistory.length - 1],
      predicted: recentHistory[recentHistory.length - 1].price,
      upper1: recentHistory[recentHistory.length - 1].price,
      lower1: recentHistory[recentHistory.length - 1].price,
      upper2: recentHistory[recentHistory.length - 1].price,
      lower2: recentHistory[recentHistory.length - 1].price,
    };

    return [...recentHistory.slice(0, -1), bridgePoint, ...predictionPoints];
  }, [data]);

  const todayDate = new Date().toISOString().split('T')[0];

  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload?.length) return null;
    return (
      <div style={{
        background: '#1f2937', border: '1px solid #374151',
        borderRadius: 8, padding: '12px 16px', fontSize: 12
      }}>
        <p style={{color: '#9ca3af', marginBottom: 6}}>{label}</p>
        {payload.map((p, i) => p.value != null && (
          <p key={i} style={{color: p.color, margin: '2px 0'}}>
            {p.name}: <strong>{typeof p.value === 'number' ? p.value.toFixed(2) : p.value}</strong>
          </p>
        ))}
      </div>
    );
  };

  // Show every Nth label so x-axis isn't crowded
  const tickInterval = Math.floor(chartData.length / 8);

  return (
    <div className="card">
      <h3>{data.symbol} — Historical Price + Prediction</h3>
      <div style={{marginBottom: 8, fontSize: 12, color: '#9ca3af'}}>
        Showing last 90 days + {futureDays}-day prediction with ±1σ (68%) and ±2σ (95%) confidence bands
      </div>
      <ResponsiveContainer width="100%" height={420}>
        <ComposedChart data={chartData} margin={{top: 10, right: 20, left: 10, bottom: 5}}>
          <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
          <XAxis
            dataKey="date"
            tick={{fontSize: 10, fill: '#6b7280'}}
            interval={tickInterval}
            tickFormatter={d => d.slice(5)} // show MM-DD
          />
          <YAxis
            tick={{fontSize: 11, fill: '#6b7280'}}
            tickFormatter={v => v.toFixed(0)}
            domain={['auto', 'auto']}
          />
          <Tooltip content={<CustomTooltip />} />
          <Legend wrapperStyle={{fontSize: 12}} />

          {/* Today line - divides historical from prediction */}
          <ReferenceLine x={todayDate} stroke="#f59e0b" strokeDasharray="4 4"
            label={{value: 'Today', fill: '#f59e0b', fontSize: 11}} />

          {/* ±2σ confidence band (outer, 95%) */}
          <Area dataKey="upper2" name="+2σ (95%)" fill="#1e3a5f" stroke="none" fillOpacity={0.4} />
          <Area dataKey="lower2" name="-2σ (95%)" fill="#0a0e1a" stroke="none" fillOpacity={1} />

          {/* ±1σ confidence band (inner, 68%) */}
          <Area dataKey="upper1" name="+1σ (68%)" fill="#1d4ed8" stroke="none" fillOpacity={0.35} />
          <Area dataKey="lower1" name="-1σ (68%)" fill="#0a0e1a" stroke="none" fillOpacity={1} />

          {/* SMA line */}
          <Line dataKey="sma" name="Moving Avg (20d)" stroke="#f59e0b"
            dot={false} strokeWidth={1.5} strokeDasharray="4 2" connectNulls />

          {/* Actual historical price */}
          <Line dataKey="price" name="Actual Price" stroke="#10b981"
            dot={false} strokeWidth={2} connectNulls />

          {/* Predicted price (median) */}
          <Line dataKey="predicted" name="Predicted Price" stroke="#3b82f6"
            dot={false} strokeWidth={2} strokeDasharray="6 3" connectNulls />
        </ComposedChart>
      </ResponsiveContainer>

      {/* R² explanation */}
      <div style={{marginTop: 12, padding: '10px 14px', background: 'rgba(59,130,246,0.05)',
        border: '1px solid rgba(59,130,246,0.15)', borderRadius: 8, fontSize: 12, color: '#9ca3af'}}>
        <strong style={{color: '#93c5fd'}}>📐 Model Stats: </strong>
        R² = {(data.regression?.rSquared * 100)?.toFixed(1)}% &nbsp;|&nbsp;
        σ (daily) = {data.regression?.sigma?.toFixed(4)} &nbsp;|&nbsp;
        Annualised volatility = {(data.regression?.annualisedVolatility * 100)?.toFixed(1)}% &nbsp;|&nbsp;
        Std Dev = {data.standardDeviation}
        <br />
        <span style={{marginTop: 4, display:'block'}}>
          R² measures how well the regression line fits — closer to 100% means stronger trend.
          σ is the standard deviation of residuals (A-Level Statistics).
        </span>
      </div>
    </div>
  );
}
