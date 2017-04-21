import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;



public class Factory 
{
	StandardAnalyzer englishAnalyzer = new StandardAnalyzer();
	Vector<String> tokens = new Vector<String>();
	Database db  = new Database();
	HashMap<String, Integer> lexion = new HashMap<String,Integer>();
	Stemmer stemmer = new Stemmer();

	
	public Factory() {
		db.connect();
	}
	
	public boolean Tokenize (String page) {
		try {
			TokenStream tokenizer = englishAnalyzer.tokenStream(null, page);
			tokenizer.reset();
			CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
			while (tokenizer.incrementToken()){
				tokens.addElement(attr.toString());
			}
			tokenizer.end();
			tokenizer.close();
			return true;
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean Index(String url) {
		try {
			SiteHits site;
			System.out.println("indexing an new page");
			System.out.println("tokens size before start: "+tokens.size());
			System.out.println("");
			for (int i=0;i<tokens.size();i++){
				String st = tokens.elementAt(i);
				site = new SiteHits(st,stemmer.action(st),url);
				int pos;
				while (tokens.contains(st)){
					pos = tokens.indexOf(st);
					site.addHit(pos, "body");
					tokens.remove(st);
				}
				if (lexion.containsKey(st)){
					System.out.println("found word "+ st + " in the lexion");
					db.updateDocument(site);
				}else {
					lexion.put(st, 0);
					System.out.println("added word "+ st + " in the lexion");
					db.createDocument(site);
				}
			}
			return true;
		} catch (Exception exp){
			System.out.println("Error while indexing a word " + exp.getMessage());
			System.out.println("");
			return false;
		}
	}
	
	public void close () {
		englishAnalyzer.close();
	}
}












