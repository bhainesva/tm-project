import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * Created by adityabindra on 4/16/16.
 */
public class Analyzer {

    int m_N; // N-gram size to be used; set to 1 for unigram

    DocumentPreprocessor documentPreprocessor; // Stanford NLP preprocessor
    Tokenizer m_tokenizer; // Stanford tokenizer

    HashSet<String> m_stopwords; // words not to be included in vocabulary

    HashSet<Document> m_documents; // documents of all FOMC Minutes

    LanguageModel m_LM; // Language Model for the purposes of this exploration

    public Analyzer(int n_gram) {
        m_N = n_gram;
        //documentPreprocessor = new DocumentPreprocessor();
    }

    public static void main(String[] args) throws IOException {
        String sample = "The quick brown fox jumped over the brown fence.";
        // option #1: By sentence.
        DocumentPreprocessor dp = new DocumentPreprocessor(sample);
        for (List<HasWord> sentence : dp) {
            System.out.println(sentence);
        }
        // option #2: By token
//        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(arg),
//                new CoreLabelTokenFactory(), "");
//        while (ptbt.hasNext()) {
//            CoreLabel label = ptbt.next();
//            System.out.println(label);
//        }
    }

}
