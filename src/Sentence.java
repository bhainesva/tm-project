import edu.stanford.nlp.pipeline.Annotation;

import java.util.HashMap;

/**
 * Created by adityabindra on 4/26/16.
 */
public class Sentence {

    int m_id; // the numerical ID assigned to this Sentence
    public int getID() {
        return m_id;
    }
    public void setID(int id) {
        this.m_id = id;
    }

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

    double m_sentiment;
    public double getSentiment() { return m_sentiment; }
    public void setSentiment(double sentiment) { m_sentiment = sentiment; }

    Annotation m_annotation;
    public Annotation getAnnotation() { return m_annotation; }
    public void setAnnotation(Annotation annotation) { m_annotation = annotation; }

    public Sentence(double sentiment, Annotation annotation) {
        m_id = -1;
        m_sentiment = sentiment;
        m_annotation = annotation;
        m_content = annotation.toString();
    }

}
