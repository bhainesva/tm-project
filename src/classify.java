import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.classify.ColumnDataClassifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class classify{
	LexicalizedParser lp;
    int blank;
    ColumnDataClassifier classifier;
    
    public String parse(List<HasWord> text){
    	List<CoreLabel> text1=Sentence.toCoreLabelList(text);    	
    	String np="";
    	Tree parse_w=lp.apply(text1);
    	for (Tree sub:parse_w){
    		if(sub.label().value().equals("NP") & sub.depth()==2){      			
    			blank=1;
    			for (Tree noun:sub){
    				if (noun.label().value().equals("NN")||noun.label().value().equals("NNP")){
    						
    						np=np+" "+noun.yield().get(0).toString(); 
    						blank=0;
    					}
    					
    					}
    				if (blank==0)
    					np=np+"\t";
    				
    			
    			}
    	
    		}
    	return (np);
    	
    }
	public String create_features(String file) throws IOException{
		 Properties props = new Properties();	     
	     DocumentPreprocessor doc=new DocumentPreprocessor(file);
	     String tokens="\t";
	     for(List<HasWord> sentence:doc){	    	 
			tokens=tokens+parse(sentence);
	     }	     
	     return (tokens);
	     
	
	}
	public void LoadDir(String path){
		File dir=new File(path);
		String eol=System.getProperty("line.separator");
		int i=0;
		try(Writer writer=new FileWriter("data/train.txt"))
		{
		for(File f:dir.listFiles()){
			if (f.isFile()){
				i++;
				System.out.println("document no " + Integer.toString(i));
				writer.append(f.getParentFile().getName())
					.append(create_features(f.getAbsolutePath()))
					.append(eol);
			}
			else
				LoadDir(f.getAbsolutePath());
				
		}
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public classify(String train_path){
		lp=LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    	
		LoadDir(train_path);
	}
	public classify(String train_path,String type){	
		Properties props = new Properties();
        props.setProperty("trainFile",train_path);
    	classifier=new ColumnDataClassifier(props);
		
	}
	
	public static void main(String[] args) throws IOException {
		//String[] t={"hello","consumer","price","inflation","has","risen","."};
		//parse(Sentence.toWordList(t));
		
		//classify classifier=new classify("data/bloomberg/sample");
		classify classifier=new classify("data/train.txt","train");
	}
}