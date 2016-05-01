import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by adityabindra on 4/16/16.
 */
public class SentimentAnalysis {

    Properties m_props; // Stanford properties
    StanfordCoreNLP m_pipeline;

    DocumentPreprocessor documentPreprocessor; // Stanford NLP preprocessor
    PTBTokenizer m_tokenizer; // Stanford PTBT tokenizer
    TokenizerFactory<CoreLabel> m_tokenizerFactory;

    HashSet<String> m_savedDocuments;

    public SentimentAnalysis() {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing,
        // and coreference resolution
        m_props = new Properties();
        m_props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        m_pipeline = new StanfordCoreNLP(m_props);
        m_tokenizerFactory = m_tokenizer.factory(new CoreLabelTokenFactory(), "");
    }

    public void LoadDirectory(String folder, String suffix)
            throws IOException {
        System.out.println("Reading FOMC files from " + folder + "...");

        if (m_savedDocuments == null) {
            m_savedDocuments = new HashSet<String>();
            LoadSavedDocumentNames("data/fomc_minutes_sentiment/", ".txt");
        }

        File dir = new File(folder);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(suffix)) {
                System.out.println(file.getName());
                long startTime = System.currentTimeMillis();

                if (!m_savedDocuments.contains(file.getName().replaceAll(".txt", ""))) {
                    Document document = ReadFOMCMinutes(file);
                    Analyzer.getDocuments().add(document);
                    SaveDocumentSentiment(document, "data/fomc_minutes_sentiment/");
                } else System.out.println("Already have saved sentiments of " + file.getName().replaceAll(".txt", "") + " to data/fomc_minutes_sentiment/");

                long endTime = System.currentTimeMillis();
                System.out.println("Ran in " + (endTime - startTime) / 1000.0 + " seconds");
            } else if (file.isDirectory())
                LoadDirectory(file.getAbsolutePath(), suffix);
        }

        System.out.println();
        System.out.println("Analyzed " + Analyzer.getDocuments().size() + " FOMC Minutes from " + folder);
        System.out.println();
    }

    public void LoadSentimentDocuments(String folder, String suffix) throws IOException {
        System.out.println("Reading FOMC files pre-calculated sentence sentiments from " + folder + "...");

        File dir = new File(folder);
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(suffix)) {
                System.out.println(file.getName());
                Document document = ReadFOMCSentimentMinutes(file);
                Analyzer.getDocuments().add(document);
            } else if (file.isDirectory())
                LoadSentimentDocuments(file.getAbsolutePath(), suffix);
        }

        for (Document document : Analyzer.getDocuments()) {
            System.out.println(document.getDateStr());
            System.out.println("==========================================");
            for (Sentence sentence : document.getSentences()) {
                System.out.println(sentence.getSentiment() + ": " + sentence.getContent());
            }
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

    public Document ReadFOMCMinutes(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Document document = new Document(file.getName().replaceAll(".txt", ""));
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
                iterator.setText(line);
                int start = iterator.first();
                for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                    String sentenceText = line.substring(start,end);
                    Annotation sentence = new Annotation(sentenceText);
                    m_pipeline.annotate(sentence);
                    document.addSentence(findSentiment(sentence), sentence);
                }
            }
        }
        reader.close();

        return document;
    }

    public Document ReadFOMCSentimentMinutes(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Document document = new Document(file.getName().replaceAll(".txt", ""));
        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                String[] data = line.split("\t");
                double sentiment = Double.parseDouble(data[0]);
                String sentence = data[1];
                document.addSentence(sentiment, sentence);
            }
        }
        reader.close();

        return document;
    }

    public void SaveDocumentSentiment(Document document, String folder) throws IOException {
        System.out.println("Saving " + document.getDateStr() + " sentiments to " + folder + "...");

        File file = new File(folder + document.getDateStr() + ".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        System.out.println(file.getName());
        for (Sentence sentence : document.getSentences()) {
            writer.append(sentence.getSentiment() + "\t" + sentence.getContent() + "\n");
        }
        writer.flush();
        writer.close();

        System.out.println();
        System.out.println("Saved sentiments of " + Analyzer.getDocuments().size() + " documents to " + folder);
    }

    public double findSentiment(Annotation sentence) {
        double sentiment = 0;
        for (CoreMap line : sentence.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = line.get(SentimentAnnotatedTree.class);
            sentiment += RNNCoreAnnotations.getPredictedClass(tree);
        }

        // Scale to [-1, 1]
        if (sentiment == 0) sentiment = -1.0;
        else if (sentiment == 1) sentiment = -0.5;
        else if (sentiment == 2) sentiment = 0.0;
        else if (sentiment == 3) sentiment = 0.5;
        else if (sentiment == 4) sentiment = 1.0;
        else System.out.println("ERROR: " + sentiment + " is not a valid sentiment class!");

        return sentiment;
    }

    public String SnowballStemming(String token) {
        SnowballStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(token);
        if (stemmer.stem()) return stemmer.getCurrent();
        else return token;
    }

    public String Normalization(String token) {
        // convert to lower case
        token = token.toLowerCase();
        // remove all punctuation
        token = token.replaceAll("\\p{Punct}", "");
        // group all numerical tokens together
        token = token.replaceAll("\\d+(\\.?\\d+|\\.?)", "NUM");
        return token;
    }

    public static void main(String[] args) throws IOException {
        SentimentAnalysis sentiment_analyzer = new SentimentAnalysis();

        //sentiment_analyzer.LoadDirectory("data/fomc_minutes_sample/", ".txt");
        sentiment_analyzer.LoadDirectory("data/fomc_minutes/", ".txt");

        //sentiment_analyzer.LoadSentimentDocuments("data/fomc_minutes_sentiment", ".txt");


    }

}