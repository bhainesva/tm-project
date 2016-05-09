import java.io.IOException;
import java.text.ParseException;
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
    public void setStocks(Document document, Date endDate) throws IOException, ParseException {
        if (document == null) System.out.println("doc is null!");
        for (String sector : document.getExcessAvgSentiments().keySet()) {
            if (document.getExcessAvgSentiments().get(sector) > 0.0
                    && !sector.equalsIgnoreCase("financial_services")
                    && !sector.equalsIgnoreCase("real_estate")) {
                addStock(Backtester.getSPDR(sector),
                        new Stock(Backtester.getSPDR(sector), sector,
                                document.getExcessAvgSentiments().get(sector),
                                m_date, endDate));
            }
        }
    }
    public void DisplayStocksPretty() {
        for (String ticker : m_stocks.keySet()) {
            System.out.println(ticker + "[" + m_stocks.get(ticker).getSector() + "]: "
                    + m_stocks.get(ticker).getWeight() * 100
                    + "%; $" + m_stocks.get(ticker).getStartAdjClose()
                    + " -> $" + m_stocks.get(ticker).getEndAdjClose()
                    + "; " + m_stocks.get(ticker).getOrders() + " shares");
        }
    }

    public void CalcWeights() {
        CalcTotSentiment();
        for (String ticker : m_stocks.keySet()) {
            m_stocks.get(ticker).setWeight(m_stocks.get(ticker).getSentiment() / m_totSentiment);
        }
    }

    public void CalcOrders() {
        for (String ticker : m_stocks.keySet()) {
            m_stocks.get(ticker).setOrders(m_stocks.get(ticker).getWeight() * m_startMarketValue
                    / m_stocks.get(ticker).getStartAdjClose());
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
    public void CalcEndMarketValue() {
        double marketValue = 0.0;
        for (String ticker : m_stocks.keySet()) {
            marketValue += m_stocks.get(ticker).getOrders() * m_stocks.get(ticker).getEndAdjClose();
        }
        setEndMarketValue(marketValue);
    }

    double m_return; // returns from the period from this portfolio to the next
    public double getReturn() { return m_return; }
    public void setReturn(double returnVal) { m_return = returnVal; }
    public void CalcReturn() { m_return = (m_endMarketValue - m_startMarketValue) / m_startMarketValue; }

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
