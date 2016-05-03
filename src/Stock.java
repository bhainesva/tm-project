import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public double getAdjClose(Date lookupDate)
            throws IOException, ParseException {
        lookupDate = nearestWeekday(lookupDate);

        File file = new File(System.getProperty("user.home") + "/Google Drive/Tick Data/Daily/"
                + m_ticker + ".csv");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        double close = 0;
        int counter = 0; // used to skip header
        //System.out.println("Looking for " + lookupDate.toString());
        while ((line = br.readLine()) != null) {
            if (counter > 0) {
                String[] data = line.split(",");
                Date tempDate = df.parse(data[0]);
                //System.out.println(tempDate.toString());
                Double adjClose = Double.parseDouble(data[6]);
                if (lookupDate.equals(tempDate)) {
                    //System.out.println(ticker + ": " + adjClose);
                    close = adjClose;
                    return close;
                }
            }
            counter++;
        }

        System.out.println("NO TICK DATA FOUND FOR " + m_ticker + " ON " + lookupDate.toString());
        return close;
    }

    public static Date nearestWeekday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Start date is on a Saturday
        if (cal.get(cal.DAY_OF_WEEK) == 7) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal.getTime();
        }
        // Start date is on a Sunday
        else if (cal.get(cal.DAY_OF_WEEK) == 1) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        } else {
            return date;
        }
    }

    public Stock(String ticker, String sector, double sentiment, Date startDate, Date endDate) throws IOException, ParseException {
        setTikcer(ticker);
        setSector(sector);
        setSentiment(sentiment);
        setStartAdjClose(getAdjClose(startDate));
        setEndAdjClose(getAdjClose(endDate));
    }

}
