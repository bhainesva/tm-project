import java.util.Date;
import java.util.HashMap;

public class Portfolio implements Comparable<Portfolio> {

    Date m_date;
    public Date getDate() { return m_date; }
    public void setDate(Date date) { m_date = date; }

    HashMap<String, Stock> m_stocks;
    public HashMap<String, Stock> getStocks() { return m_stocks; }
    public void addStock(String ticker, Stock stock) { m_stocks.put(ticker, stock); }
    public void setStocks(HashMap<String, Stock> stocks) { m_stocks = stocks; }
    public void setStocks(Document document) {
        if (document == null) System.out.println("doc is null!");
        for (String sector : document.getExcessAvgSentiments().keySet()) {
            if (document.getExcessAvgSentiments().get(sector) > 0.0) {
                addStock(Backtester.getSPDR(sector),
                        new Stock(Backtester.getSPDR(sector), sector, document.getExcessAvgSentiments().get(sector)));
            }
        }
        setWeights();
    }
    public void setWeights() {
        CalcTotSentiment();
        for (String ticker : m_stocks.keySet()) {
            m_stocks.get(ticker).setWeight(m_stocks.get(ticker).getSentiment() / m_totSentiment);
        }
    }
    public void DisplayStocksPretty() {
        for (String ticker : m_stocks.keySet()) {
            System.out.println(ticker + "[" + m_stocks.get(ticker).getSector() + "]: "
                    + m_stocks.get(ticker).getWeight() * 100 + "%");
        }
    }

    public double m_totSentiment;
    public double getTotSentiment() { return m_totSentiment; }
    public void CalcTotSentiment() {
        for (String ticker : m_stocks.keySet()) {
            m_totSentiment += m_stocks.get(ticker).getSentiment();
        }
    }

    double m_startMarketValue;
    public double getStartMarketValue() { return m_startMarketValue; }
    public void setStartMarketValue(double marketValue) { m_startMarketValue = marketValue; }

    double m_endMarketValue;
    public double getEndMarketValue() { return m_endMarketValue; }
    public void setEndMarketValue(double marketValue) { m_endMarketValue = marketValue; }

    public Portfolio(Date date) {
        setDate(date);
        m_stocks = new HashMap<String, Stock>();
    }

    @Override
    public int compareTo(Portfolio o) {
        if (getDate() == null || o.getDate() == null)
            return 0;
        return getDate().compareTo(o.getDate());
    }
}
