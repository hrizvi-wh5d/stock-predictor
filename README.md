# 📈 StockPredictor — A-Level Mathematics Stock Analysis App

A full-stack web application built with **React + Spring Boot** that lets users predict stock prices using **A-Level mathematics** (Linear Regression, Moving Averages, Standard Deviation).

---

## 🧮 A-Level Maths Used

| Technique | A-Level Topic | Purpose |
|---|---|---|
| Simple Moving Average | Data/Statistics | Smooths historical price noise |
| Linear Regression (log-linear) | Statistics — Regression | Projects price trend into future |
| Standard Deviation (σ) | Statistics — Normal Distribution | Measures price volatility |
| Confidence Intervals (±1σ, ±2σ) | Statistics — Normal Distribution | 68% and 95% prediction bands |
| R² Coefficient | Statistics — Correlation | Measures how well the model fits |

### Key Formulas

```
SMA(n) = (P₁ + P₂ + ... + Pₙ) / n

Linear Regression:  ŷ = a + bx
  b = (nΣxy - ΣxΣy) / (nΣx² - (Σx)²)
  a = (Σy - bΣx) / n

Standard Deviation:  σ = √(Σ(xᵢ - x̄)² / n)

R² = 1 - (SS_res / SS_tot)
```

> Why log prices? Stock prices grow multiplicatively, not additively. Taking log() converts this to additive growth, making linear regression mathematically valid.

---

## 🚀 How to Run in GitHub Codespaces (Recommended)

### Step 1 — Upload to GitHub

1. Go to [github.com](https://github.com) and create a **new repository** (e.g. `stock-predictor`)
2. Upload all these project files keeping the same folder structure
3. Or use Git:

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/stock-predictor.git
git push -u origin main
```

### Step 2 — Open in Codespaces

1. On your GitHub repo page, click the green **`<> Code`** button
2. Click **`Codespaces`** tab
3. Click **`Create codespace on main`**
4. Wait ~2 minutes while it installs Java 17, Node 18, and all dependencies automatically

### Step 3 — Run the Backend

In the Codespaces terminal:

```bash
cd backend
mvn spring-boot:run
```

Wait for: `✅ Stock Predictor Backend is running on http://localhost:8080`

### Step 4 — Run the Frontend

Open a **second terminal** (click the `+` icon):

```bash
cd frontend
npm start
```

The browser will auto-open with the app running! 🎉

### Step 5 — View the Database (Optional, great for learning!)

Visit: `https://YOUR-CODESPACE-URL-8080.app.github.dev/h2-console`

- JDBC URL: `jdbc:h2:mem:stockdb`
- Username: `sa`
- Password: *(leave blank)*

---

## 🌐 How to Deploy Publicly (Free)

### Option A — Railway (Easiest, recommended)

1. Go to [railway.app](https://railway.app) and sign in with GitHub
2. Click **New Project → Deploy from GitHub Repo**
3. Select your `stock-predictor` repo
4. Railway auto-detects Spring Boot and deploys the backend
5. For the frontend: add another service → select the `frontend` folder
6. Both get public HTTPS URLs!

### Option B — Render (Also Free)

**Backend:**
1. Go to [render.com](https://render.com) → New → Web Service
2. Connect your GitHub repo
3. Root directory: `backend`
4. Build command: `mvn clean package -DskipTests`
5. Start command: `java -jar target/stock-predictor-0.0.1-SNAPSHOT.jar`

**Frontend:**
1. New → Static Site
2. Root directory: `frontend`
3. Build command: `npm run build`
4. Publish directory: `build`
5. Set environment variable: `REACT_APP_API_URL=https://your-backend.onrender.com`

### Option C — GitHub Pages (Frontend only)

```bash
cd frontend
npm install gh-pages --save-dev
npm run build
npx gh-pages -d build
```

---

## 📁 Project Structure

```
stock-predictor/
├── .devcontainer/
│   └── devcontainer.json          ← Codespaces config (auto-installs everything)
├── backend/
│   ├── pom.xml                    ← Java dependencies (like package.json)
│   └── src/main/java/com/stockpredictor/
│       ├── StockPredictorApplication.java   ← App entry point
│       ├── controller/
│       │   ├── AuthController.java          ← /api/auth/login, /register
│       │   ├── StockController.java         ← /api/stocks/history, /predict
│       │   └── UserController.java          ← /api/user/profile
│       ├── model/
│       │   └── User.java                    ← Database table definition
│       ├── repository/
│       │   └── UserRepository.java          ← Database queries
│       ├── security/
│       │   ├── JwtUtils.java                ← JWT token generation/validation
│       │   └── JwtAuthFilter.java           ← Request interceptor
│       ├── service/
│       │   ├── PredictionService.java       ← A-LEVEL MATHS ENGINE ⭐
│       │   └── UserDetailsServiceImpl.java  ← Spring Security user loader
│       └── config/
│           └── SecurityConfig.java          ← Auth rules & CORS config
└── frontend/
    └── src/
        ├── context/AuthContext.js           ← Global login state
        ├── pages/
        │   ├── LoginPage.js
        │   ├── RegisterPage.js
        │   ├── DashboardPage.js             ← Main app screen
        │   └── ProfilePage.js
        └── components/
            └── StockChart.js                ← Interactive prediction chart
```

---

## 🔐 How Authentication Works

```
1. User registers → password is BCrypt hashed → saved to H2 database
2. User logs in → Spring Security verifies hash → server generates JWT token
3. Frontend stores token in localStorage
4. Every API request includes: Authorization: Bearer <token>
5. JwtAuthFilter validates token on every request
6. Token expires after 24 hours → user must log in again
```

---

## 📊 How the Prediction Works

```
1. Fetch last 12 months of daily closing prices from Yahoo Finance
2. Calculate 20-day Simple Moving Average across all points
3. Take log() of all prices (converts multiplicative to additive growth)
4. Apply least-squares linear regression to find best-fit line
5. Project the regression line N days into the future
6. Convert back from log space: price = e^(predicted_log_price)
7. Calculate σ (standard deviation of residuals)
8. Draw ±1σ band (68% confidence) and ±2σ band (95% confidence)
```

---

## 🛠 Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Frontend | React 18 | Component-based UI |
| Charts | Recharts | SVG charts in React |
| HTTP | Axios | API calls with auth headers |
| Routing | React Router v6 | Multi-page app |
| Backend | Spring Boot 3.2 | Java REST API framework |
| Auth | Spring Security + JWT | Industry-standard auth |
| Database | H2 (in-memory) | Zero setup for development |
| Data | Yahoo Finance API | Free, no API key needed |
| Deployment | GitHub Codespaces | Browser-based dev environment |
