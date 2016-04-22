import java.util.Date;
import java.util.HashMap;

/**
 * Created by adityabindra on 4/16/16.
 */
public class Document {

    int m_id; // the numerical ID assigned to this FOMC Minutes
    public int getID() {
        return m_id;
    }
    public void setID(int id) {
        this.m_id = id;
    }

    Date m_date; // FOMC meeting date
    public void setDate(String dateStr) {

    }
    public Date getDate() { return m_date; }

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

    public Document() {
        m_id = -1;
        m_date = null;
        m_content = null;
        m_tokens = null;
        m_vector = new HashMap<String, Token>();
    }

    public Document(int id) {
        m_id = id;
        m_date = null;
        m_content = null;
        m_tokens = null;
        m_vector = new HashMap<String, Token>();
    }

}
