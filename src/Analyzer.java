import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.stats.Counter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Analyzer {

    static HashSet<Document> m_documents; // documents of all FOMC Minutes
    public static HashSet<Document> getDocuments() {
        if (m_documents == null) m_documents = new HashSet<Document>();
        return m_documents;
    }

    Classifier<String, String> m_classifier;
    ColumnDataClassifier m_columnDataClassifier;
    static LexicalizedParser m_lp;
    public static LexicalizedParser getLP() { return m_lp; }

    public Analyzer() throws IOException {
        SentimentAnalysis sentiment_analyzer = new SentimentAnalysis();
        sentiment_analyzer.LoadSentimentDocuments("data/fomc_minutes_sentiment", ".txt");

        DocumentClassification doc_classifier = new DocumentClassification("data/noun_phrases/train_sent.txt","train");
        m_classifier = doc_classifier.getClassifer();
        m_columnDataClassifier = doc_classifier.getCDclassifier();
        m_lp = doc_classifier.getLP();

        if (m_lp == null) System.out.println("LP null");
    }

    public void analyze() {

        for (Document document : m_documents) {
            System.out.println(document.getDateStr());
            System.out.println("========================================");
            for (Sentence sentence : document.getSentences()) {
              //String sentiment = m_classifier.classOf(m_columnDataClassifier.makeDatumFromLine(sentence.getParsedNouns()));
                Counter<String> sentiment = m_classifier.scoresOf(m_columnDataClassifier.makeDatumFromLine(sentence.getParsedNouns()));
              System.out.println(softmax(sentiment) + ": " + sentence.getContent());
                System.out.println();
            }
            System.out.println();
            System.out.println();
            System.out.println();
        }
    }

    public HashMap<String, Double> softmax(Counter<String> scores) {
        Double sum = scores.entrySet()
                .stream()
                .map(x -> Math.exp(x.getValue()))
                .reduce((a, b) -> a + b).get();

        HashMap<String, Double> probs = new HashMap<String, Double>
                (scores.entrySet()
                        .stream()
                        .collect(Collectors.toMap(x -> x.getKey(), x -> Math.exp(x.getValue()) / sum))
                );

        return (probs);
    }

    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new Analyzer();
        analyzer.analyze();
    }

}
