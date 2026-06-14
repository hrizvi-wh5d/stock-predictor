import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import StockChart from '../components/StockChart';

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const [market, setMarket] = useState('NASDAQ');
  const [stocks, setStocks] = useState([]);
  const [selectedStock, setSelectedStock] = useState(null);
  const [stockData, setStockData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [futureDays, setFutureDays] = useState(30);
  const [targetDate, setTargetDate] = useState('');
  const [error, setError] = useState('');

  // Load stock list when market changes
  useEffect(() => {
    axios.get(`/api/stocks/list?market=${market}`)
      .then(res => setStocks(res.data))
      .catch(err => console.error(err));
  }, [market]);

  // When user sets a target date, calculate days from today
  const handleDateChange = (date) => {
    setTargetDate(date);
    if (date) {
      const today = new Date();
      const future = new Date(date);
      const days = Math.ceil((future - today) / (1000 * 60 * 60 * 24));
      if (days > 0) setFutureDays(days);
    }
  };

  const handleStockSelect = async (stock) => {
    setSelectedStock(stock);
    setStockData(null);
    setError('');
    setLoading(true);
    try {
      const res = await axios.get(`/api/stocks/predict?symbol=${stock.symbol}&futureDays=${futureDays}`);
      setStockData(res.data);
    } catch (err) {
      setError('Failed to fetch stock data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handlePredict = async () => {
    if (!selectedStock) return;
    setLoading(true);
    setError('');
    try {
      const res = await axios.get(`/api/stocks/predict?symbol=${selectedStock.symbol}&futureDays=${futureDays}`);
      setStockData(res.data);
    } catch (err) {
      setError('Prediction failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const regression = stockData?.regression;
  const currentPrice = stockData?.currentPrice;
  const predictedFinalPrice = regression?.predictions?.[regression.predictions.length - 1]?.predicted;
  const priceChange = predictedFinalPrice && currentPrice
    ? ((predictedFinalPrice - currentPrice) / currentPrice * 100).toFixed(2)
    : null;

  return (
    <div>
      {/* Navbar */}
      <nav className="navbar">
        <span className="navbar-brand">📈 StockPredictor</span>
        <div className="navbar-actions">
          <span className="navbar-user">👤 {user?.username}</span>
          <Link to="/profile" className="btn btn-outline" style={{padding:'6px 12px',fontSize:'13px'}}>Profile</Link>
          <button className="btn btn-outline" onClick={logout} style={{padding:'6px 12px',fontSize:'13px'}}>Logout</button>
        </div>
      </nav>

      <div className="dashboard">
        <div className="dashboard-grid">
          {/* Left panel - stock list */}
          <div>
            <div className="card">
              <h3>Select Market</h3>
              <div className="market-tabs">
                {['NASDAQ', 'FTSE'].map(m => (
                  <button key={m} className={`market-tab ${market === m ? 'active' : ''}`}
                    onClick={() => setMarket(m)}>{m}</button>
                ))}
              </div>
              <h3>Stocks</h3>
              {stocks.map(stock => (
                <div key={stock.symbol}
                  className={`stock-item ${selectedStock?.symbol === stock.symbol ? 'active' : ''}`}
                  onClick={() => handleStockSelect(stock)}>
                  <div className="symbol">{stock.symbol}</div>
                  <div className="name">{stock.name}</div>
                </div>
              ))}
            </div>

            {/* A-Level maths explanation */}
            <div className="maths-box" style={{marginTop: 16}}>
              <h4>📐 A-Level Maths Used</h4>
              <div className="formula">SMA(n) = (P₁ + P₂ + ... + Pₙ) / n</div>
              <div className="formula">ŷ = a + bx  (Linear Regression)</div>
              <div className="formula">b = (nΣxy - ΣxΣy) / (nΣx² - (Σx)²)</div>
              <div className="formula">σ = √(Σ(xᵢ - x̄)² / n)</div>
              <p>The prediction uses log-linear regression on historical closing prices.
                Confidence bands show ±1σ (68%) and ±2σ (95%) intervals — A-Level Normal Distribution.</p>
            </div>
          </div>

          {/* Right panel - chart */}
          <div>
            {/* Prediction controls */}
            {selectedStock && (
              <div className="card" style={{marginBottom: 16}}>
                <h3>Prediction Controls</h3>
                <div className="chart-controls">
                  <div className="control-group">
                    <label>Target Date</label>
                    <input type="date" value={targetDate}
                      min={new Date().toISOString().split('T')[0]}
                      onChange={e => handleDateChange(e.target.value)} />
                  </div>
                  <div className="control-group">
                    <label>or Days Ahead</label>
                    <input type="number" value={futureDays} min="1" max="365"
                      onChange={e => setFutureDays(parseInt(e.target.value))} />
                  </div>
                  <button className="btn btn-primary" style={{width:'auto'}}
                    onClick={handlePredict} disabled={loading}>
                    {loading ? '⏳ Predicting...' : '🔮 Run Prediction'}
                  </button>
                </div>
              </div>
            )}

            {/* Stats row */}
            {stockData && (
              <div className="stats-row">
                <div className="stat-card">
                  <div className="label">Current Price</div>
                  <div className="value neutral">£/$ {currentPrice?.toFixed(2)}</div>
                </div>
                <div className="stat-card">
                  <div className="label">Predicted ({futureDays}d)</div>
                  <div className="value neutral">£/$ {predictedFinalPrice?.toFixed(2)}</div>
                </div>
                <div className="stat-card">
                  <div className="label">Expected Change</div>
                  <div className={`value ${parseFloat(priceChange) >= 0 ? 'positive' : 'negative'}`}>
                    {priceChange >= 0 ? '+' : ''}{priceChange}%
                  </div>
                </div>
                <div className="stat-card">
                  <div className="label">R² (Fit Quality)</div>
                  <div className="value neutral">{(regression?.rSquared * 100)?.toFixed(1)}%</div>
                </div>
              </div>
            )}

            {/* Chart */}
            {error && <div className="error-msg">{error}</div>}
            {loading && <div className="loading">⏳ Fetching data & running prediction...</div>}
            {!selectedStock && !loading && (
              <div className="empty-state">
                <h3>Select a stock to begin</h3>
                <p>Choose a stock from the left panel to view historical data and predictions</p>
              </div>
            )}
            {stockData && !loading && <StockChart data={stockData} futureDays={futureDays} />}
          </div>
        </div>
      </div>
    </div>
  );
}
