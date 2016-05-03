import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.stats.Counter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Analyzer {

    static HashMap<String, Document> m_documents; // documents of all FOMC Minutes
    public static HashMap<String, Document> getDocuments() {
        if (m_documents == null) m_documents = new HashMap<String, Document>();
        return m_documents;
    }
    public static Document getDocument(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMd-yyyy");
        if (m_documents.containsKey(formatter.format(date).toLowerCase())) return m_documents.get(formatter.format(date).toLowerCase());
        else {
            System.out.println("ERROR: no document with " + formatter.format(date).toLowerCase() + " exists!");
            return null;
        }
    }

    HashSet<String> m_savedDocuments;

    Classifier<String, String> m_classifier;
    ColumnDataClassifier m_columnDataClassifier;
    static LexicalizedParser m_lp;
    public static LexicalizedParser getLP() { return m_lp; }

    public Analyzer() throws IOException {
        SentimentAnalysis sentiment_analyzer = new SentimentAnalysis();
        sentiment_analyzer.LoadSentimentDocuments("data/fomc_minutes_sentiment", ".txt");
    }

    public void LoadDocumentClassifications(String folder, String suffix) throws IOException, ParseException {
        System.out.println("Reading FOMC files pre-calculated sentence sentiments from " + folder + "...");

        File dir = new File(folder);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(suffix)) {
                String document = file.getName().replaceAll(".txt", "");
                if (m_documents.containsKey(document)) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (m_documents.get(document).getSentences().containsKey(line)) {
                            String content = line;
                            String[] classifications = reader.readLine().split("\t");
                            HashMap<String, Double> sectorClassifications = new HashMap<String , Double>();
                            for (int i = 0; i < classifications.length - 1; i+=2) {
                                sectorClassifications.put(classifications[i], Double.parseDouble(classifications[i + 1]));
                            }
                            m_documents.get(document).getSentences().get(content).setClassifications(sectorClassifications);
                            m_documents.get(document).addSentiment(m_documents.get(document).getSentences().get(content).getSentiment(),
                                    m_documents.get(document).getSentences().get(content).getClassifications());
                            m_documents.get(document).addClassification(m_documents.get(document).getSentences().get(content).getClassifications());

                            /*
                            System.out.println(content);
                            System.out.println(m_documents.get(document).getSentences().get(content).getSentiment() + ": "
                                    + m_documents.get(document).getSentences().get(content).getClassifications());
                            System.out.println();
                            */
                        }
                    }
                    m_documents.get(document).CalcAvgSentiments();
                    m_documents.get(document).CalcAvgClassifications();
                    m_documents.get(document).CalcExcessAvgSentiments();

                    SimpleDateFormat formatter = new SimpleDateFormat("MMMdd-yyyy");
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
                    m_documents.get(document).setDate(formatter.parse(m_documents.get(document).getDateStr()));
                    System.out.println(displayFormat.format(m_documents.get(document).getDate())
                            + "[" + document + "]");
                    System.out.println("========================================");

                    m_documents.get(document).DisplayAvgSentimentsPretty();
                    System.out.println();
                    m_documents.get(document).DisplayAvgClassificationsPretty();
                    System.out.println();
                    m_documents.get(document).DisplayExcessAvgSentimentsPretty();

                    System.out.println();
                    System.out.println();
                    System.out.println();
                } else System.out.println("ERROR: " + document + " does not have saved sentence classifications!");
            } else if (file.isDirectory())
                LoadDocumentClassifications(file.getAbsolutePath(), suffix);
        }

        System.out.println();
        System.out.println("Loaded " + Analyzer.getDocuments().size() + " FOMC Minutes from " + folder);
        System.out.println();
    }

    public void LoadSavedDocumentNames(String folder, String suffix) {
        System.out.println();
        System.out.println("Reading FOMC Minutes titles from " + folder + "...");

        File dir = new File(folder);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(suffix)) {
                System.out.println(file.getName().replaceAll(".txt", ""));
                m_savedDocuments.add(file.getName().replaceAll(".txt", ""));
            } else if (file.isDirectory())
                LoadSavedDocumentNames(file.getAbsolutePath(), suffix);
        }
        System.out.println();

    }

    public void SaveDocumentClassifications(String folder) throws IOException {
        // Initializations needed to get sentence classifications
        DocumentClassification doc_classifier = new DocumentClassification("data/noun_phrases/train_sent.txt","train");
        m_classifier = doc_classifier.getClassifer();
        m_columnDataClassifier = doc_classifier.getCDclassifier();
        m_lp = doc_classifier.getLP();

        if (m_savedDocuments == null) {
            m_savedDocuments = new HashSet<String>();
            LoadSavedDocumentNames(folder, ".txt");
        }

        System.out.println("Saving sentence classifications for all documents to " + folder + "...");
        for (String document : m_documents.keySet()) {
            if (!m_savedDocuments.contains(document)) {
                long startTime = System.currentTimeMillis();
                File file = new File(folder + document + ".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                System.out.println(file.getName());
                System.out.println("========================================");

                for (String sentence : m_documents.get(document).getSentences().keySet()) {
                    Counter<String> sentiment = m_classifier.scoresOf(m_columnDataClassifier
                            .makeDatumFromLine(m_documents.get(document).getSentences().get(sentence).getParsedNouns()));
                    m_documents.get(document).getSentences().get(sentence).setClassifications(softmax(sentiment));

                    writer.append(sentence + "\n");
                    for (String sector : m_documents.get(document).getSentences().get(sentence).getClassifications().keySet()) {
                        writer.append(sector + "\t" + m_documents.get(document).getSentences().get(sentence).getProbability(sector) + "\t");
                    }
                    writer.newLine();
                }
                writer.flush();
                writer.close();

                long endTime = System.currentTimeMillis();
                System.out.println("Ran in " + (endTime - startTime) / 1000.0 + " seconds");
            } else System.out.println("Already saved sentiment classifications for " + document);

            System.out.println();
            System.out.println();
            System.out.println();
        }

        System.out.println();
        System.out.println("Saved sentence classifications of " + Analyzer.getDocuments().size() + " documents to " + folder);
        System.out.println();
    }

    public void SaveDocumentData(String filename) throws IOException, ParseException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));

        writer.append(",financials,consumer_staples,materials,health_care,consumer_discretionary," +
                "technology,financial_services,utilities,industrials,energy,real_estate");
        writer.newLine();

        for (String document : m_documents.keySet()) {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy");
            writer.append(displayFormat.format(m_documents.get(document).getDate()) + ",");
            for (String sector : m_documents.get(document).getExcessAvgSentiments().keySet()) {
                writer.append(m_documents.get(document).getExcessAvgSentiments().get(sector) + ",");
            }
            writer.newLine();
        }

        writer.flush();
        writer.close();

        System.out.println("Wrote all document calculations to " + filename);

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

        return probs;
    }

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new Analyzer();

        //analyzer.SaveDocumentClassifications("data/fomc_minutes_classifications/");

        analyzer.LoadDocumentClassifications("data/fomc_minutes_classifications/", ".txt");

        //analyzer.SaveDocumentData("data/final_run.csv");

    }

}
