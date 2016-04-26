import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


public class sentiment_analysis {

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static void main(String[] args) throws IOException {


        // One method to train the sentiment model
//        List<Tree> trainingTrees = SentimentUtils.readTreesWithGoldLabels("./data/trainingInput.txt");  // The sentence trees with corrected labels
//        SentimentModel model = SentimentModel.loadSerialized("edu/stanford/nlp/models/sentiment/sentiment.ser.gz");
//
//        String modelPath = "outputModel.ser";
//
//        SentimentTraining.train(model, modelPath, trainingTrees, null);
//        model.saveSerialized(modelPath);

        // Another Training Approach
        // In terminal in the stanford directory run to get the training input in the right format
        // java -cp "*" edu.stanford.nlp.sentiment.BuildBinarizedDataset -sentimentModel edu/stanford/nlp/models/sentiment/sentiment.ser.gz -input traininginput.txt > trainingInput.txt
        // Then to train the model run
        // java -cp "*" -mx8g edu.stanford.nlp.sentiment.SentimentTraining -numHid 25 -trainPath actualtraining.txt -train -model anothermodel.ser.gz
        // SentimentModel model = SentimentModel.loadSerialized("model-0001-0.00.ser.gz");


        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
        // Pick one of the output models to load
        //leave commented for default model
        // props.setProperty("sentiment.model", "MODELHERE.ser.gz");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = readFile("../data/fmoc_minutes/apr24-2012", StandardCharsets.UTF_8); // Add your text here!

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                //String ne = token.getString(CoreAnnotations.NamedEntityTagAnnotation.class);
                String ner = token.ner();

                //uncomment this for individual word sentiments
//                String sent = token.get(SentimentCoreAnnotations.SentimentClass.class);
//                System.out.println(sent + ": " + token);
            }

            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            System.out.println(sentiment + "\t" + sentence);

        }
    }
}



