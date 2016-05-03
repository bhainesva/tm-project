import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Backtester {

    ArrayList<Portfolio> m_portfolios;
    public ArrayList<Portfolio> getPortfolios() { return m_portfolios; }
    public void addPortfolio(Portfolio portfolio) { m_portfolios.add(portfolio); }
    public void setPortfolios(ArrayList<Portfolio> portfolios) { m_portfolios = portfolios; }
    public void DisplayPortfoliosPretty() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
        for (Portfolio portfolio : m_portfolios) {
            System.out.println("Portfolio " + displayFormat.format(portfolio.getDate()) + ": ");
            System.out.println("=====================");
            System.out.println();
            System.out.println("Initial Market Value = " + portfolio.getStartMarketValue());
            System.out.println();
            portfolio.DisplayStocksPretty();

            System.out.println();
            System.out.println();
            System.out.println();
        }
    }

    static HashMap<String, String> m_sectorMapping;
    public static HashMap<String, String> getSectorMapping() { return m_sectorMapping; }
    public void setSectorMappings() {
        m_sectorMapping = new HashMap<String, String>();
        m_sectorMapping.put("energy", "XLE");
        m_sectorMapping.put("utilities", "XLU");
        m_sectorMapping.put("technology", "XLK");
        m_sectorMapping.put("materials", "XLB");
        m_sectorMapping.put("consumer_staples", "XLP");
        m_sectorMapping.put("consumer_discretionary", "XLY");
        m_sectorMapping.put("industrials", "XLI");
        m_sectorMapping.put("health_care", "XLV");
        m_sectorMapping.put("financials", "XLF");
        m_sectorMapping.put("financial_services", "XLFS");
        m_sectorMapping.put("real_estate", "XLRE");
    }
    public static String getSPDR(String sector) {
        if (m_sectorMapping.containsKey(sector))
            return m_sectorMapping.get(sector);
        else {
            System.out.println("ERROR: " + sector + " does not map to a SPDR!");
            return null;
        }
    }

    public void LoadPortfolios(double initialCapital) {
        for (String document : Analyzer.getDocuments().keySet()) {
            addPortfolio(new Portfolio(Analyzer.getDocuments().get(document).getDate()));
        }
        Collections.sort(m_portfolios);

        for (int i = 0; i < m_portfolios.size(); i++) {
            if (i == 0) m_portfolios.get(i).setStartMarketValue(initialCapital);

            m_portfolios.get(i).setStocks(Analyzer.getDocument(m_portfolios.get(i).getDate()));
        }
    }

    public Backtester(double intialCapital) throws IOException, ParseException {
        Analyzer analyzer = new Analyzer();
        analyzer.LoadDocumentClassifications("data/fomc_minutes_classifications/", ".txt");

        m_portfolios = new ArrayList<Portfolio>();
        setSectorMappings();
        LoadPortfolios(intialCapital);
    }

    public static void main(String[] args) throws IOException, ParseException {
        Backtester backtester = new Backtester(10000.00);

        backtester.DisplayPortfoliosPretty();

    }

}
