
public class Stock {

    String m_ticker;
    public String getTicker() { return m_ticker; }
    public void setTikcer(String ticker) { m_ticker = ticker; }

    String m_sector;
    public String getSector() { return m_sector; }
    public void setSector(String sector) { m_sector = sector; }

    double m_sentiment;
    public double getSentiment() { return m_sentiment; }
    public void setSentiment(double sentiment) { m_sentiment = sentiment; }

    double m_weight;
    public double getWeight() { return m_weight; }
    public void setWeight(double weight) { m_weight = weight; }

    double m_orders;
    public double getOrders() { return m_orders; }
    public void setOrders(double orders) { m_orders = orders; }

    double m_startAdjClose;
    public double getStartAdjClose() { return m_startAdjClose; }
    public void setStartAdjClose(double adjClose) { m_startAdjClose = adjClose; }

    double m_endAdjClose;
    public double getEndAdjClose() { return m_endAdjClose; }
    public void setEndAdjClose(double adjClose) { m_endAdjClose = adjClose; }

    public Stock(String ticker, String sector, double sentiment) {
        setTikcer(ticker);
        setSector(sector);
        setSentiment(sentiment);
    }

}
