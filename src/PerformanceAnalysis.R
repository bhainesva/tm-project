### Backtesting results

# Libraries
library(PerformanceAnalytics)
library(reshape2)
library(ggplot2)
library(quantmod)

# Workspace path
path <-  "/Users/adityabindra/Google Drive/Tick Data/"

dailyReturns <- function(ticker) {
  returns <-  read.csv(paste(path, "Daily/", ticker, ".csv", sep = ""))
  returns <- returns[-c(2:6)] # Keep date and adj_close data only
  colnames(returns) <- c("Date", "adj_close")
  returns$Date <- as.Date(returns$Date, format = "%Y-%m-%d")
  returns$Daily.Returns <- c(0, diff(returns$adj_close)/returns$adj_close[-length(returns$adj_close)])
  returns <- returns[-1, ]
  return(returns)
}

setLength <- function(ticker) {
  return <- ticker[2768:nrow(ticker),]
}

GSPC <- setLength(dailyReturns("^GSPC"))
XLE <- setLength(dailyReturns("XLE"))
XLU <- setLength(dailyReturns("XLU"))
XLK <- setLength(dailyReturns("XLK"))
XLB <- setLength(dailyReturns("XLB"))
XLP <- setLength(dailyReturns("XLP"))
XLY <- setLength(dailyReturns("XLY"))
XLI <- setLength(dailyReturns("XLI"))
XLV <- setLength(dailyReturns("XLV"))
XLF <- setLength(dailyReturns("XLF"))
#XLFS <- dailyReturns("XLFS")
#XLRE <- dailyReturns("XLRE")
Strategy <- read.csv("../data/strategy_returns.csv")

returns <- data.frame(GSPC$Date, GSPC$Daily.Returns, 
                      XLE$Daily.Returns,
                      XLU$Daily.Returns,
                      XLK$Daily.Returns,
                      XLB$Daily.Returns,
                      XLP$Daily.Returns,
                      XLY$Daily.Returns,
                      XLI$Daily.Returns,
                      XLV$Daily.Returns,
                      XLF$Daily.Returns)
#XLFS$Daily.Returns,
#XLRE$Daily.Returns)
colnames(returns) <- c("Date", "GSPC", "XLE", "XLU", "XLK", "XLB", "XLP", "XLY", "XLI", "XLV", "XLF")
returns.zoo <- read.zoo(returns, format = "%Y-%m-%d")
returns <- melt(returns, id.vars = 1, variable.name = "Ticker", value.name = "Daily.Return")

# Charting
charts.PerformanceSummary(returns.zoo, legend.loc = "bottomleft", colorset = rainbow(10, s = .75, v = .75), lwd = 2)



















