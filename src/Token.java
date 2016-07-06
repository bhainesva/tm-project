public class Token {

    int m_id; // the numerical ID assigned to this token
    public int getID() {
        return m_id;
    }
    public void setID(int id) {
        this.m_id = id;
    }

    String m_token; // the actual text of this token
    public String getToken() {
        return m_token;
    }
    public void setToken(String token) {
        this.m_token = token;
    }

    // number of times this token occurs in a document d
    int m_TF; // raw (non-normalized) term frequency of this token
    public int getTF() {
        return m_TF;
    }
    public void incrementTF() {
        m_TF++;
    }

    double m_TF_norm; // normalized TF of this token
    public double getTF_norm() { return m_TF_norm; }
    public void setTF_norm(double TF_norm) { this.m_TF_norm = TF_norm; }

    int m_TTF; // total term frequency of this token
    public int getTTF() { return m_TTF; }
    public void setTTF(int ttf) { m_TTF = ttf; }
    public void incrementTTF() { m_TTF++; }

    // number of documents this token occurs in a corpus of documents
    int m_DF; // document frequency of this token
    public int getDF() { return m_DF; }
    public void incrementDF() { m_DF++; }

    double m_IDF; // inverse document frequency of this token
    public double getIDF() { return this.m_IDF; }
    public void setIDF(double idf) { this.m_IDF = idf; }

    double m_TFIDF; // TF-IDF of this token
    public double getTFIDF() { return this.m_TFIDF; }
    public void setTFIDF(double TFIDF) { this.m_TFIDF = TFIDF; }

    double m_p; // probability of this token
    public double getP() { return m_p; }
    public void setP(double p) { m_p = p; }

    public Token(String token) {
        m_token = token;
        m_id = -1;
        m_TF = 1;
        m_DF = 1;
        m_TTF = 1;
        m_TFIDF = 0.0;
    }

    public Token(int id, String token) {
        m_token = token;
        m_id = id;
        m_TF = 1;
        m_DF = 1;
        m_TTF = 1;
        m_TFIDF = 0.0;
    }

    @Override
    public String toString() {
        return "Token{" +
                "m_token=\"" + m_token + '\"' +
                ", m_id=" + m_id +
                ", m_TF=" + m_TF +
                ", m_TF_norm=" + m_TF_norm +
                ", m_TTF=" + m_TTF +
                ", m_DF=" + m_DF +
                ", m_IDF=" + m_IDF +
                ", m_TFIDF=" + m_TFIDF +
                ", m_p=" + m_p +
                '}';
    }
}