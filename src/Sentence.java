import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.trees.Tree;
import org.tartarus.snowball.ext.porterStemmer;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    public String m_parsedNouns;
    public String getParsedNouns() {
        if (m_parsedNouns == null || m_parsedNouns.isEmpty()) setParsedNouns();;
        return m_parsedNouns;
    }
    public void setParsedNouns() {
        m_parsedNouns = parse_nouns(getHasWords(m_content));
    }

    public HashMap<String, Double> m_classifications; // probability this Sentence applies to any of the 12 sectors
    public HashMap<String, Double> getClassifications() { return m_classifications; }
    public void setClassifications(HashMap<String, Double> classifications) { m_classifications = classifications; }
    public double getProbability(String sector) {
        if (m_classifications.containsKey(sector)) {
            return m_classifications.get(sector);
        } else System.out.println("ERROR: " + sector + " is not a valid sector!");
        return 0.0;
    }

    public Sentence(double sentiment, Annotation annotation) {
        m_id = -1;
        m_sentiment = sentiment;
        m_annotation = annotation;
        m_content = annotation.toString();
    }

    public Sentence(double sentiment, String content) {
        m_id = -1;
        m_sentiment = sentiment;
        m_annotation = null;
        m_content = content;
    }

    // function to extract only the noun phrases from each sentence of document
    public String parse_nouns(List<HasWord> text) {
        List<CoreLabel> text1 = edu.stanford.nlp.ling.Sentence.toCoreLabelList(text);
        System.out.println(text1);
        String np = "";
        Tree parse_w = Analyzer.getLP().apply(text1);
        int blank = 0;

        //iterating through the parse tree to identify noun phrases and nouns within the noun phrases
        for (Tree sub : parse_w) {
            if (sub.label().value().equals("NP") & sub.depth() == 2) {
                blank = 1;
                for (Tree noun : sub) {
                    if (noun.label().value().equals("NN") || noun.label().value().equals("NNP")) {
                        np = np + " " + PorterStemming(noun.yield().get(0).toString()).toLowerCase();
                        blank = 0;
                    }
                }
                if (blank == 0) np = np + "\t";
            }
        }
        return (np);
    }

    public String PorterStemming(String token) {
        porterStemmer stemmer = new porterStemmer();
        stemmer.setCurrent(token);
        if (stemmer.stem()) return stemmer.getCurrent();
        else return token;
    }


    public List<HasWord> getHasWords(String content) {
        Reader reader = new StringReader(m_content);
        DocumentPreprocessor doc = new DocumentPreprocessor(reader);
        for (List<HasWord> sentence : doc) {
            return sentence;
        }
        System.out.println("ERROR: " + doc + " is empty!");
        return null;
    }

}
