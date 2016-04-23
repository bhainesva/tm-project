import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.GeneralDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.InputStreamReader;


public class classify{
	LexicalizedParser lp;
    int blank;
    ColumnDataClassifier CDclassifier;
    HashSet<String> stopwords;
    Classifier<String,String> cl;
    
    public void LoadStopwords(String filename) {
		try {stopwords=new HashSet<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;
			
			while ((line = reader.readLine()) != null) {
										
				if (!line.isEmpty())
					stopwords.add(line.toLowerCase());
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
    
    //function to extract only the noun phrases from each sentence of document
    public String parse_nouns(List<HasWord> text){
    	List<CoreLabel> text1=Sentence.toCoreLabelList(text);    	
    	String np="";
    	Tree parse_w=lp.apply(text1);
    	//iterating through the parse tree to identify noun phrases and nouns within the noun phrases
    	for (Tree sub:parse_w){
    		if(sub.label().value().equals("NP") & sub.depth()==2){      			
    			blank=1;
    			for (Tree noun:sub){
    				if (noun.label().value().equals("NN")||noun.label().value().equals("NNP")){
    						
    						np=np+" "+noun.yield().get(0).toString().toLowerCase(); 
    						blank=0;
    					}
    					
    					}
    				if (blank==0)
    					np=np+"\t";
    				
    			
    			}
    	
    		}
    	return (np);
    	
    }
    
    //as an alternative to the above function, this returns all the words in the text after removing
    // stopwords and lowercased.
    public String tokenize(List<HasWord> text){
    	String token_s;
    	String words="";
    	for(HasWord token:text){
    		token_s=token.word().replaceAll("[^A-Za-z ]", "").toLowerCase();
    		if (!stopwords.contains(token_s) & !token_s.equals("") & token_s.length()<20)
    			words=words+token_s+"\t";
    			
    	}
    	return(words);
    }
    
    //create features for each document
	public String create_features(String file) throws IOException{
		 Properties props = new Properties();	     
	     DocumentPreprocessor doc=new DocumentPreprocessor(file);
	     String tokens="\t";
	     for(List<HasWord> sentence:doc){	    	 
			tokens=tokens+parse_nouns(sentence);
	     }	     
	     return (tokens);
	     
	
	}
	public void LoadDir(String path){
		File dir=new File(path);
		String eol=System.getProperty("line.separator");
		int i=0;
		
		for(File f:dir.listFiles()){
			if (f.isFile()){
				i++;
				System.out.println("document no " + Integer.toString(i));
				try(Writer writer=new FileWriter("data/train.txt",true))
				{
				writer.append(f.getParentFile().getName())
					.append(create_features(f.getAbsolutePath()))
					.append(eol);
				} catch(IOException e){
					e.printStackTrace();}
			}
			else
				LoadDir(f.getAbsolutePath());
				
		}
		
		}
	
	//to create tab delimited training text files 
	public classify(String train_path){
		lp=LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    	LoadStopwords("data/english.stop.txt");
		LoadDir(train_path);
	}
	
	public void cross_validate(String path){
		Properties props = new Properties();
		props.setProperty("featureFormat","true"); // to be used in case text file has features and not text
		props.setProperty("crossValidationFolds", "3");
		props.setProperty("printCrossValidationDecisions", "true");
		props.setProperty("shuffleTrainingData", "true");
		props.setProperty("displayAllAnswers", "true");
		ColumnDataClassifier CVclassifier=new ColumnDataClassifier(props);		
    	Pair<GeneralDataset<String,String>,List<String[]>> features=CVclassifier.readTestExamples(path);
    	Pair<Double,Double> metrics=CVclassifier.crossValidate(features.first(), features.second());
		
		
	}
	
	public void test(String path){
		Pair<GeneralDataset<String,String>,List<String[]>> test=CDclassifier.readTestExamples(path);
		for (RVFDatum<String, String> line:test.first()){
			System.out.println(cl.scoresOf(line));
			//System.out.println(line.toString());
		}
		  
		
	}
	
	// to use the text file to train the classifier
	public classify(String train_path,String type){	
		Properties props = new Properties();
		
		props.setProperty("featureFormat","true"); // to be used in case text file has features and not text				
		props.setProperty("displayAllAnswers", "true");
		
		//below 3 props to be used in case  text file has the actual text with class
		//props.setProperty("lowercase", "true");
		//props.setProperty("1.splitWordsTokenizerRegexp", "[\\p{L}][\\p{L}0-9]*|(?:\\$ ?)?[0-9]+(?:\\.[0-9]{2})?%?|\\s+|[\\x80-\\uFFFD]|.");
		//props.setProperty("1.useLowercaseSplitWords", "true");
		//props.setProperty("1.useNGrams", "true");
        //props.setProperty("trainFile",train_path);
		
    	CDclassifier=new ColumnDataClassifier(props);
    	
    	if (type.equals("train")){
    		GeneralDataset<String,String> train_data=CDclassifier.readTrainingExamples("data/train.txt");
    		cl=CDclassifier.makeClassifier(train_data);    		 	
    	}
    	else
    		cross_validate(train_path);
    	
        
	}
	
	public static void main(String[] args) throws IOException {
		//String[] t={"hello","consumer","price","inflation","has","risen","."};
		//parse(Sentence.toWordList(t));
		
		//classify classifier=new classify("data/bloomberg");
		classify classifier=new classify("data/train.txt","train");
		classifier.test("data/test.txt");
	}
}