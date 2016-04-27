import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.trees.Tree;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.io.InputStreamReader;
import org.tartarus.snowball.ext.porterStemmer;


public class DocumentClassification {
	LexicalizedParser lp;
    int blank;
    ColumnDataClassifier CDclassifier;
    HashSet<String> stopwords;
    Classifier<String,String> cl;
    
    public String PorterStemming(String token) {
		porterStemmer stemmer = new porterStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
    
    public void LoadStopwords(String filename) {
		try {stopwords=new HashSet<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;
			
			while ((line = reader.readLine()) != null) {
										
				if (!line.isEmpty())
					stopwords.add(PorterStemming(line).toLowerCase());
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
    	System.out.println(text1);
    	String np="";
    	Tree parse_w=lp.apply(text1);
    	//iterating through the parse tree to identify noun phrases and nouns within the noun phrases
    	for (Tree sub:parse_w){
    		if(sub.label().value().equals("NP") & sub.depth()==2){      			
    			blank=1;
    			for (Tree noun:sub){
    				if (noun.label().value().equals("NN")||noun.label().value().equals("NNP")){
    						np=np+" "+ PorterStemming(noun.yield().get(0).toString()).toLowerCase();     						
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
    		token_s=PorterStemming(token.word().replaceAll("[^A-Za-z ]", "").toLowerCase());
    		if (!stopwords.contains(token_s) & !token_s.equals("") & token_s.length()<20)
    			words=words+token_s+"\t";
    			
    	}
    	return(words);
    }
    
    //create features for each document
	public void create_features(String file,String label) throws IOException{		     
	     DocumentPreprocessor doc=new DocumentPreprocessor(file);
	     String eol=System.getProperty("line.separator");	    
	     String tokens;
	     
	     try(Writer writer=new FileWriter("data/noun_phrases/train_sent.txt",true))
			{
		     for(List<HasWord> sentence:doc){	    	 
				tokens=parse_nouns(sentence);				
				if (tokens.length()>10)
					
					writer.append(label)
						  .append("\t")
						.append(tokens)
						.append(eol);
					
			}
		} catch(IOException e){
			e.printStackTrace();}
		
	}
	public void LoadDir(String path,String type) throws IOException{
		File dir=new File(path);
		String eol=System.getProperty("line.separator");
		int i=0;
		
		for(File f:dir.listFiles()){
			if (f.isFile()){
				i++;
				System.out.println("document no " + Integer.toString(i));	
				if (type.equals("train"))
					create_features(f.getAbsolutePath(),f.getParentFile().getName());
				else
					parse_test(f.getAbsolutePath());
			}
			else
				LoadDir(f.getAbsolutePath(),type);
		     }
		
		}
	
	
	
	public void cross_validate(String path){
		Properties props = new Properties();
		props.setProperty("featureFormat","true"); // to be used in case text file has features and not text
		props.setProperty("crossValidationFolds", "10");
		//props.setProperty("printCrossValidationDecisions", "true");
		props.setProperty("shuffleTrainingData", "true");
		props.setProperty("displayAllAnswers", "true");
		ColumnDataClassifier CVclassifier=new ColumnDataClassifier(props);		
    	Pair<GeneralDataset<String,String>,List<String[]>> features=CVclassifier.readTestExamples(path);
    	Pair<Double,Double> metrics=CVclassifier.crossValidate(features.first(), features.second());
    	System.out.println(metrics.first()+" "+metrics.second());
		
		
	}
	
	public HashMap<String,Double> softmax(Counter<String> scores){
		Double sum=scores.entrySet()
				.stream()
				.map(x->Math.exp(x.getValue()))
				.reduce((a,b)->a+b).get();
		HashMap<String,Double> probs=new HashMap<String,Double>
										(scores.entrySet()
											.stream()
											.collect(Collectors.toMap(x->x.getKey(), x->Math.exp(x.getValue())/sum))
											);
		return(probs);

	}
	
	
	
	public void test(String path) throws IOException{	
		lp=LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    	LoadStopwords("data/english.stop.txt");
		LoadDir(path,"test");		
		}
		
	public void parse_test(String path){
			DocumentPreprocessor doc=new DocumentPreprocessor(path);
			Datum<String,String> tokens;
			for(List<HasWord> sentence:doc){
				if (sentence.size()<40){
					tokens=CDclassifier.makeDatumFromLine(parse_nouns(sentence));	
					System.out.println(tokens.labels());
					System.out.println(softmax(cl.scoresOf(tokens)));}				
			
		}
	
		 
		
		
	}
	//to create tab delimited training text files 
		public DocumentClassification(String train_path) throws IOException{
			lp=LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	    	LoadStopwords("data/english.stop.txt");
			LoadDir(train_path,"train");
		}
	
	// to use the text file to train the classifier
	public DocumentClassification(String train_path, String type){
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
    		GeneralDataset<String,String> train_data=CDclassifier.readTrainingExamples(train_path);
    		cl=CDclassifier.makeClassifier(train_data);    		 	
    	}
    	else
    		cross_validate(train_path);
    	
        
	}
	
	public static void main(String[] args) throws IOException {
		//String[] t={"hello","consumer","price","inflation","has","risen","."};
		//parse(Sentence.toWordList(t));
		
		//DocumentClassification classifier=new DocumentClassification("data/bloomberg/");
		DocumentClassification classifier = new DocumentClassification("data/noun_phrases/train.txt","train");
		//classifier.test("data/fmoc_minutes/test");
		//DocumentClassification classifier=new DocumentClassification("data/noun_phrases/train_sent.txt","cv");
		
	}
}