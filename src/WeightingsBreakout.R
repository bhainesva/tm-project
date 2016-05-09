### Aditya Bindra
### Weighting Breakout over Backtest

# Load the needed libraries
require(ggplot2)
require(reshape2)

weightings <- read.csv("/Users/adityabindra/Google\ Drive/UVa/2nd\ Year/CS\ 6501/tm-project/data/strategy_weightings.csv",
                     header = TRUE)
weightings$Date <- as.Date(weightings$Date, format = "%m/%d/%Y")
weightings <- weightings[5:nrow(weightings),]

theme <- theme(title = element_text(face = "bold", size = 16), 
               axis.title = element_text(face="bold", size=16), 
               axis.text = element_text(size = 14), 
               legend.text = element_text(size = 12),
               legend.position = "bottom",
               legend.title=element_blank())

plot <- ggplot(weightings, aes(x = Date, y = Weighting, group = Sector, fill = Sector)) + 
  geom_area(position = "fill") + theme
plot
