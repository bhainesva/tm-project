### Backtesting results

# Libraries
library(PerformanceAnalytics)
library(reshape2)
library(ggplot2)
library(quantmod)
library(zoo)

# Set risk-free rate from the 10-Y Treasury Note on 01/25/2011
# 39 period backtest
Rf = 0.62/(39)

strategy <- read.csv("/Users/adityabindra/Google\ Drive/UVa/2nd\ Year/CS\ 6501/tm-project/data/strategy_returns.csv",
                         header = TRUE)
strategy$Date <- as.Date(strategy$Date, format = "%m/%d/%Y")

# Workspace path
path <-  "/Users/adityabindra/Google Drive/Tick Data/"

returns <- function(ticker, model) {
  data <-  read.csv(paste(path, "Daily/", ticker, ".csv", sep = ""))
  data <- data[-c(2:6)] # Keep date and adj_close data only
  colnames(data) <- c("Date", "adj_close")
  data$Date <- as.Date(data$Date, format = "%Y-%m-%d")
  data <- merge(data, model, by = 'Date')
  data$Benchmark.Returns <- c(0, diff(data$adj_close)/data$adj_close[-length(data$adj_close)])
  data <- data[-1, ] # delete extra row
  #View(data)
  return(data[ , !(names(data) %in% "adj_close")])
}

GSPC <- returns("^GSPC", strategy)
XLE <- returns("XLE", strategy)
XLU <- returns("XLU", strategy)
XLK <- returns("XLK", strategy)
XLB <- returns("XLB", strategy)
XLP <- returns("XLP", strategy)
XLY <- returns("XLY", strategy)
XLI <- returns("XLI", strategy)
XLV <- returns("XLV", strategy)
XLF <- returns("XLF", strategy)
#XLFS <- dailyReturns("XLFS")
#XLRE <- dailyReturns("XLRE")

returns <- data.frame(GSPC$Date, GSPC$Return,
                      GSPC$Benchmark.Returns)
returns.all <- data.frame(GSPC$Date, GSPC$Return,
                      GSPC$Benchmark.Returns, 
                      XLE$Benchmark.Returns,
                      XLU$Benchmark.Returns,
                      XLK$Benchmark.Returns,
                      XLB$Benchmark.Returns,
                      XLP$Benchmark.Returns,
                      XLY$Benchmark.Returns,
                      XLI$Benchmark.Returns,
                      XLV$Benchmark.Returns,
                      XLF$Benchmark.Returns)
#XLFS$Daily.Returns,
#XLRE$Daily.Returns)

colnames(returns) <- c("Date", "Strategy", "S.P.500")
#colnames(returns.all) <- c("Date", "Strategy", "GSPC", "XLE", "XLU", "XLK", "XLB", "XLP", "XLY", "XLI", "XLV", "XLF")
returns <- read.zoo(returns, format = "%Y-%m-%d")
#returns.all.zoo <- read.zoo(returns.all, format = "%Y-%m-%d")

#################### PERFORMANCE METRICS ####################
# Annualized Information Ratio
returns.InformationRatio <- InformationRatio(returns, returns$S.P.500)

# Beta against the S&P 500
returns.BetaCoVariance <- BetaCoVariance(returns, returns$S.P.500)

# Modigliani RAP
returns.Modigliani <- Modigliani(returns, returns$S.P.500, Rf = Rf)

# Tracking Error
returns.TrackingError <- TrackingError(returns, returns$S.P.500)

# CAPM Alpha
returns.CAPM.alpha <- CAPM.alpha(returns, returns$S.P.500, Rf = Rf)

# Active Premium
returns.ActivePremium <- ActivePremium(returns, returns$S.P.500)

# Matrix of all performance metrics
returns.performance.metrics <- as.data.frame(rbind(returns.InformationRatio, returns.BetaCoVariance, 
                                                   returns.Modigliani, returns.TrackingError,
                                                   returns.CAPM.alpha, returns.ActivePremium))

#################### RISK METRICS ####################
# SD Annualized
returns.sd.annualized <- sd.annualized(returns)

# VaR
returns.VaR <- VaR(returns, method = "historical")

# Drawdowns
returns.MaxDrawdowns <- maxDrawdown(returns)

# Matrix of all risk metrics
returns.risk.metrics <- as.data.frame(rbind(returns.InformationRatio, returns.MaxDrawdowns, 
                                            returns.sd.annualized, returns.VaR))

# View performance metrics
View(returns.performance.metrics)
# Write to CSV
#write.csv(returns.performance.metrics)
# View risk metrics
View(returns.risk.metrics)

#################### CHARTS ####################
# Historgrams with Fitted Normal Distributions
chart.Histogram(returns$Strategy, methods = c("add.normal"), main = "Strategy Histogram with Fitted Normal Distribution", lwd = 3, breaks = 7)
chart.Histogram(returns$S.P.500, methods = c("add.normal"), main = "S&P 500 Histogram with Fitted Normal Distribution", lwd = 3, breaks = 7)

# Charting
charts.PerformanceSummary(returns, legend.loc = "topleft", colorset = c("#2ac74c", "black"), lwd = 3)
#charts.PerformanceSummary(returns.all.zoo, legend.loc = "bottomleft", colorset = rainbow(11, s = .75, v = .75), lwd = 2)

# Cumulative returns
chart.CumReturns(returns, wealth.index = TRUE, legend.loc = "topleft", colorset = c("#2ac74c", "black"), lwd = 3, main = "Cumulative Returns: January 2011 - March 2016")

# Relative Performance
chart.RelativePerformance(returns, returns$S.P.500, main = "Relative Performance Against S&P 500: January 2011 - March 2016", colorset = c("#2ac74c", "black"), lwd = 3, legend.loc = "topleft")

# Correlation
chart.Correlation(returns, colorset = c("#2ac74c", "black"), lwd = 3, main = "Strategy Correlations")

# Barplot
charts.BarVaR(returns, colorset = c("#2ac74c", "black"), main = "Bar Charts of Returns")



# Risk Return Scatterplot
chart.RiskReturnScatter(returns, Rf, colorset = c("#2ac74c", "black"), lwd = 2, add.sharpe = c(1))

# Boxplot of returns
chart.Boxplot(returns, colorset = c("#2ac74c", "black"), lwd = 3, main = "Boxplot Returns: January 2011 - March 2016")

# Drawdowns
chart.Drawdown(returns, legend.loc = "bottomright", colorset = c("#2ac74c", "black"), lwd = 3, main = "Drawdowns")

var
skewness(returns)
kurtosis(returns)
CAPM.RiskPremium(returns$Strategy, Rf = Rf)
CAPM.beta.bull(returns$Strategy, returns$S.P.500, Rf = Rf)
CAPM.beta.bear(returns$Strategy, returns$S.P.500, Rf = Rf)
TimingRatio(returns$Strategy, returns$S.P.500, Rf = Rf)
InformationRatio(returns$Strategy, returns$S.P.500, scale = 8)
SharpeRatio(returns$Strategy, Rf = Rf)


















