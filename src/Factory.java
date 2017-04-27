import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Factory {
	private StandardAnalyzer englishAnalyzer = new StandardAnalyzer();
	private Vector<String> tokens = new Vector<String>();
	private Database db;
	public HashMap<String, Integer> lexion = new HashMap<String, Integer>();
	private Stemmer stemmer = new Stemmer();

	public Factory() {
		db = new Database();
	}

	public boolean Tokenize(String page) {
		try {
			TokenStream tokenizer = englishAnalyzer.tokenStream(null, page);
			tokenizer.reset();
			CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
			while (tokenizer.incrementToken()) {
				tokens.addElement(attr.toString());
			}
			tokenizer.end();
			tokenizer.close();
			return true;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public boolean Index(String url) {
		try {
			SiteHits site;
			System.out.println("indexing an new page");
			System.out.println("tokens size before start: " + tokens.size());
			System.out.println("");
			int tokens_size = tokens.size();
			for (int i = 0; i < tokens_size; i++) {
				int tf = 1;
				String st = tokens.elementAt(i);
				site = new SiteHits(st, stemmer.action(st), url);
				int pos;
				while (tokens.contains(st)) {
					tf++;
					pos = tokens.indexOf(st);
					site.addHit(pos, "body");
					tokens.remove(st);
				}
				site.setTF(tf/tokens_size);
				if (lexion.containsKey(st)) {
					lexion.put(st, lexion.get(st) + 1);
					System.out.println("found word " + st + " in the lexion");
					db.updateDocument(site);
				} else {
					lexion.put(st, 0);
					System.out.println("added word " + st + " in the lexion");
					db.createDocument(site);
				}
			}
			return true;
		} catch (Exception exp) {
			System.out.println("Error while indexing a word " + exp.getMessage());
			System.out.println("");
			return false;
		}
	}

	public HashMap<String, Integer> find(String word) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<String> arr = db.findDocument("word", word);
		arr.forEach(link -> result.put(link, 2));
		arr = db.findDocument("stemmed", word);
		arr.forEach(link -> result.put(link, result.getOrDefault(link, 0) + 1));
		return result;
	}

	public void close() {
		englishAnalyzer.close();
	}
}
