import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
            System.out.println("Market Value = " + portfolio.getStartMarketValue() + " -> " + portfolio.getEndMarketValue()
                    + " = " + portfolio.getReturn() * 100 + "%");
            System.out.println();
            portfolio.DisplayStocksPretty();

            System.out.println();
            System.out.println();
            System.out.println();
        }
    }

    static HashMap<String, String> m_sectorSPDRMapping;
    public static HashMap<String, String> getSectorSPDRMapping() { return m_sectorSPDRMapping; }
    public void setSectorSPDRMappings() {
        m_sectorSPDRMapping = new HashMap<String, String>();
        m_sectorSPDRMapping.put("energy", "XLE");
        m_sectorSPDRMapping.put("utilities", "XLU");
        m_sectorSPDRMapping.put("technology", "XLK");
        m_sectorSPDRMapping.put("materials", "XLB");
        m_sectorSPDRMapping.put("consumer_staples", "XLP");
        m_sectorSPDRMapping.put("consumer_discretionary", "XLY");
        m_sectorSPDRMapping.put("industrials", "XLI");
        m_sectorSPDRMapping.put("health_care", "XLV");
        m_sectorSPDRMapping.put("financials", "XLF");
        m_sectorSPDRMapping.put("financial_services", "XLFS");
        m_sectorSPDRMapping.put("real_estate", "XLRE");
    }
    public static String getSPDR(String sector) {
        if (m_sectorSPDRMapping.containsKey(sector))
            return m_sectorSPDRMapping.get(sector);
        else {
            System.out.println("ERROR: " + sector + " does not map to a SPDR!");
            return null;
        }
    }

    static HashMap<String, String> m_SPDRSectorMapping;
    public static HashMap<String, String> getSPDRSectorMapping() { return m_SPDRSectorMapping; }
    public void setSPDRSectorMappings() {
        m_SPDRSectorMapping = new HashMap<String, String>();
        m_SPDRSectorMapping.put("XLE", "energy");
        m_SPDRSectorMapping.put("XLU", "utilities");
        m_SPDRSectorMapping.put("XLK", "technology");
        m_SPDRSectorMapping.put("XLB", "materials");
        m_SPDRSectorMapping.put("XLP", "consumer_staples");
        m_SPDRSectorMapping.put("XLY", "consumer_discretionary");
        m_SPDRSectorMapping.put("XLI", "industrials");
        m_SPDRSectorMapping.put("XLV", "health_care");
        m_SPDRSectorMapping.put("XLF", "financials");
        m_SPDRSectorMapping.put("XLFS", "financial_services");
        m_SPDRSectorMapping.put("XLRE", "real_estate");
    }
    public static String getSector(String SPDR) {
        if (m_SPDRSectorMapping.containsKey(SPDR))
            return m_SPDRSectorMapping.get(SPDR);
        else {
            System.out.println("ERROR: " + SPDR + " does not map to a sector!");
            return null;
        }
    }

    public void LoadPortfolios(double initialCapital) throws IOException, ParseException {
        for (String document : Analyzer.getDocuments().keySet()) {
            addPortfolio(new Portfolio(Analyzer.getDocuments().get(document).getDate()));
        }
        Collections.sort(m_portfolios);

        for (int i = 0; i < m_portfolios.size(); i++) {
            if (i == 0) m_portfolios.get(i).setStartMarketValue(initialCapital);
            else m_portfolios.get(i).setStartMarketValue(m_portfolios.get(i - 1).getEndMarketValue());

            if (i < m_portfolios.size() - 1) {
                m_portfolios.get(i).setStocks(Analyzer.getDocument(m_portfolios.get(i).getDate()),
                        m_portfolios.get(i + 1).getDate());
            } else if (i == m_portfolios.size() - 1) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date endDate = displayFormat.parse("03/15/2016");
                m_portfolios.get(i).setStocks(Analyzer.getDocument(m_portfolios.get(i).getDate()), endDate);
            }
            m_portfolios.get(i).CalcWeights();
            m_portfolios.get(i).CalcOrders();
            m_portfolios.get(i).CalcEndMarketValue();
            m_portfolios.get(i).CalcReturn();
        }
    }

    public void SavePortfolios(String filename) throws IOException, ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

        writer.append("Date,Return");
        writer.newLine();
        writer.append(displayFormat.format(m_portfolios.get(0).getDate()) + ", 0.0");
        writer.newLine();
        for (int i = 0; i < m_portfolios.size(); i++) {
            if (i < m_portfolios.size() - 1) {
                writer.append(displayFormat.format(m_portfolios.get(i + 1).getDate()) + "," +
                        m_portfolios.get(i).getReturn());
            } else if (i == m_portfolios.size() - 1) {
                Date endDate = displayFormat.parse("03/15/2016");
                writer.append(displayFormat.format(endDate) + "," + m_portfolios.get(i).getReturn());
            }
            writer.newLine();
        }

        writer.flush();
        writer.close();
        System.out.println("Wrote strategy returns to " + filename);
    }

    public void SavePortfolioWeightings(String filename) throws IOException, ParseException {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

        writer.append("Date,Sector,Weighting");
        writer.newLine();
        for (String ticker : m_portfolios.get(0).getStocks().keySet()) {
            writer.append(displayFormat.format(m_portfolios.get(0).getDate()) + "," + getSector(ticker) + ",0.0");
            writer.newLine();
        }
        for (int i = 0; i < m_portfolios.size(); i++) {
            if (i < m_portfolios.size() - 1) {
                for (String ticker : m_portfolios.get(i).getStocks().keySet()) {
                    writer.append(displayFormat.format(m_portfolios.get(i + 1).getDate()) + ","
                            + getSector(ticker) + ","  + m_portfolios.get(i).getStocks().get(ticker).getWeight());
                    writer.newLine();
                }
            } else if (i == m_portfolios.size() - 1) {
                Date endDate = displayFormat.parse("03/15/2016");
                for (String ticker : m_portfolios.get(i).getStocks().keySet()) {
                    writer.append(displayFormat.format(endDate) + "," + getSector(ticker) + "," +
                            m_portfolios.get(i).getStocks().get(ticker).getWeight());
                    writer.newLine();
                }
            }
        }

        writer.flush();
        writer.close();
        System.out.println("Wrote strategy weightings to " + filename);
    }

    public Backtester(double intialCapital) throws IOException, ParseException {
        Analyzer analyzer = new Analyzer();
        analyzer.LoadDocumentClassifications("data/fomc_minutes_classifications/", ".txt");

        m_portfolios = new ArrayList<Portfolio>();
        setSectorSPDRMappings();
        setSPDRSectorMappings();
        LoadPortfolios(intialCapital);
    }

    public static void main(String[] args) throws IOException, ParseException {
        Backtester backtester = new Backtester(10000.00);

        backtester.DisplayPortfoliosPretty();
        backtester.SavePortfolios("data/strategy_returns.csv");
        backtester.SavePortfolioWeightings("data/strategy_weightings.csv");

    }

}
