import edu.stanford.nlp.pipeline.Annotation;

import java.util.Date;
import java.util.HashMap;

public class Document {

    int m_id; // the numerical ID assigned to this FOMC Minutes
    public int getID() {
        return m_id;
    }
    public void setID(int id) {
        this.m_id = id;
    }

    Date m_date; // FOMC meeting date
    public void setDate(Date date) { m_date = date; }
    public Date getDate() { return m_date; }

    String m_dateStr; // FOMC meeting date string
    public void setDateStr(String date) { m_dateStr = date; }
    public String getDateStr() { return m_dateStr; }

    String m_content; // FOMC Minutes text content
    public String getContent() {
        return m_content;
    }
    public void setContent(String content) {
        if (!content.isEmpty())
            this.m_content = content;
    }
    public boolean isEmpty() {
        return m_content==null || m_content.isEmpty();
    }

    HashMap<String, Sentence> m_sentences; // FOMC Minutes sentences in Stanford NLP format
    public HashMap<String, Sentence> getSentences() { return m_sentences; }
    public void addSentence(double sentiment, Annotation annotation) { m_sentences.put(annotation.toString(), new Sentence(sentiment, annotation)); }
    public void addSentence(double sentiment, String sentence) { m_sentences.put(sentence, new Sentence(sentiment, sentence)); }

    String[] m_tokens; // stored tokens for this FOMC Minutes
    public String[] getTokens() {
        return m_tokens;
    }
    public void setTokens(String[] tokens) {
        m_tokens = tokens;
    }

    HashMap<String, Token> m_vector; // sparse structure for storing the vector space representation with Tokens for this FOMC Minutes
    public HashMap<String, Token> getVct() {
        return m_vector;
    }
    public void setVct(HashMap<String, Token> vct) {
        m_vector = vct;
    }
    public void printVct() {
        for (String string : m_vector.keySet()) {
            System.out.println("[TF_N=" + m_vector.get(string).getTF_norm()
                    + ", IDF=" + m_vector.get(string).getIDF()
                    + ", TFIDF=" + m_vector.get(string).getTFIDF()
                    + ": " + m_vector.get(string).getToken());
        }
    }

    public HashMap<String, Double> m_totSentiments; // sum of each sector's sentiment over all sentences
    public HashMap<String, Double> getTotSentiments() { return m_totSentiments; }
    public void addSentiment(Double sentiment, HashMap<String, Double> classifications) {
        for (String sector : classifications.keySet()) {
            if (!m_totSentiments.containsKey(sector)) {
                m_totSentiments.put(sector, sentiment * classifications.get(sector));
            } else {
                double currSentiment = m_totSentiments.get(sector);
                m_totSentiments.remove(sector);
                m_totSentiments.put(sector, currSentiment + sentiment * classifications.get(sector));
            }
        }
    }

    public HashMap<String, Double> m_avgSentiments; // average of each sector's sentiment over all sentences
    public HashMap<String, Double> getAvgSentiments() { return m_avgSentiments; }
    public void CalcAvgSentiments() {
        if (m_avgSentiments == null) m_avgSentiments = new HashMap<String, Double>();
        for (String sector : m_totSentiments.keySet()) {
            m_avgSentiments.put(sector, m_totSentiments.get(sector) / m_sentences.size());
        }
    }
    public void DisplayAvgSentimentsPretty() {
        for (String sector : m_avgSentiments.keySet()) {
            System.out.println(sector + ": " + m_totSentiments.get(sector) + "/" + m_sentences.size() +
                    " = " + m_avgSentiments.get(sector));
        }
    }

    public HashMap<String, Double> m_totClassifications; // sum of each sector's classifications over all sentences
    public HashMap<String, Double> getTotClassifications() { return m_totClassifications; }
    public void addClassification(HashMap<String, Double> classifications) {
        for (String sector : classifications.keySet()) {
            if (!m_totClassifications.containsKey(sector)) {
                m_totClassifications.put(sector, classifications.get(sector));
            } else {
                double currClassification = m_totClassifications.get(sector);
                m_totClassifications.remove(sector);
                m_totClassifications.put(sector, currClassification + classifications.get(sector));
            }
        }
    }

    public HashMap<String, Double> m_avgClassifications; // average of each sector's classification over all sentences
    public HashMap<String, Double> getAvgClassifications() { return m_avgClassifications; }
    public void CalcAvgClassifications() {
        if (m_avgClassifications == null) m_avgClassifications = new HashMap<String, Double>();
        for (String sector : m_totClassifications.keySet()) {
            m_avgClassifications.put(sector, m_totClassifications.get(sector) / m_sentences.size());
        }
    }
    public void DisplayAvgClassificationsPretty() {
        for (String sector : m_avgClassifications.keySet()) {
            System.out.println(sector + ": " + m_totClassifications.get(sector) + "/" + m_sentences.size()
                    + " = " + m_avgClassifications.get(sector) * 100 + "%");
        }
    }

    public Document(String date) {
        m_id = -1;
        m_date = null;
        m_content = null;
        m_sentences = new HashMap<String, Sentence>();
        m_tokens = null;
        m_vector = new HashMap<String, Token>();
        m_dateStr = date;
        m_totSentiments = new HashMap<String, Double>();
        m_totClassifications = new HashMap<String, Double>();
    }


}