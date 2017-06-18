import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import java.util.Map;
import java.util.Iterator;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

	MongoClient mongoClient;
	MongoDatabase database;
	MongoCollection<Document> collection;
	MongoCollection<Document> coll;

	Block<Document> printBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			System.out.println(document.toJson());
		}
	};

	public Database() {
		if (!connect()) {
			System.out.println("Database failed to connect");
		}
	}

	public boolean connect() {
		try {
			mongoClient = new MongoClient();
			database = mongoClient.getDatabase("test");
			// set timeout to 3 seconds
			database.withWriteConcern(new WriteConcern(3000));
			collection = database.getCollection("col");
			coll = database.getCollection("coll");
			return true;
		} catch (Exception exp) {
			return false;
		}
	}

	public boolean createDocument(SiteHits siteHits) {
		try {
			ArrayList<Document> hits = new ArrayList<Document>();
			while (siteHits.hasHit()) {
				Pair<Integer, String> tmp = siteHits.nextHit();
				hits.add(new Document("pos", tmp.getElement0()).append("type", tmp.getElement1()));
			}
			Document doc = new Document("word", siteHits.getWord()).append("stemmed", siteHits.getStemmed())
					.append("site hits", Arrays.asList(new Document("site", siteHits.getSite())
							.append("tf", siteHits.getTF()).append("hits", hits)));
			collection.insertOne(doc);
			return true;
		} catch (Exception exp) {
			System.out.println("Error while creating document " + exp.getMessage());
			return false;
		}
	}

	public void updateDocument(SiteHits siteHits) {
		ArrayList<Document> hits = new ArrayList<Document>();
		while (siteHits.hasHit()) {
			Pair<Integer, String> tmp = siteHits.nextHit();
			hits.add(new Document("pos", tmp.getElement0()).append("type", tmp.getElement1()));
		}
		Document doc = new Document("site", "www.feedly.com").append("hits", hits);

		collection.updateOne(combine(eq("word", siteHits.getWord()), eq("site hits.site", siteHits.getSite())),
				set("site hits.$", doc));

		// collection.updateOne(eq("word","forcing"),addToSet("site hits",
		// doc));

		// collection.updateOne(
		// eq("_id", new ObjectId("57506d62f57802807471dd41")),
		// combine(set("stars", 1), set("contact.phone", "228-555-9999"),
		// currentDate("lastModified")));
	}

	public boolean deleteDocument(long id) {
		collection.deleteOne(eq("_id", new ObjectId("57506d62f57802807471dd41")));
		return true;
	}

	public HashMap<String, Integer> findDocument(String tag, String word) {
		// a query written without helpers
		// collection.find(new Document("stars", new Document("$gte", 2)
		// .append("$lt", 5))
		// .append("categories", "Bakery")).forEach(printBlock);

		// a query written with helpers
		MongoCursor<Document> cur = collection.find(eq(tag, word)).iterator();
		HashMap<String, Integer> links = new HashMap<String, Integer>();
		while (cur.hasNext()) {
			ArrayList<Document> results = (ArrayList<Document>) cur.next().get("site hits");
			results.forEach(result -> links.put(result.getString("site"), result.getInteger("tf")));
		}
		/*
		 * { links.add(result.iterator().next().getString("site")); }
		 */
		// System.out.println(cur.next().getString("site"));

		return links;
		// collection.find(and(elemMatch("list")));

		// a projection written without helpers
		// collection.find(and(gte("stars", 2), lt("stars", 5), eq("categories",
		// "Bakery")))
		// .projection(new Document("name", 1)
		// .append("stars", 1)
		// .append("categories",1)
		// .append("_id", 0))
		// .forEach(printBlock);

		// a projection written with helpers
		// collection.find(and(gte("stars", 2), lt("stars", 5), eq("categories",
		// "Bakery")))
		// .projection(fields(include("name", "stars", "categories"),
		// excludeId()))
		// .forEach(printBlock);

	}

	public void saveLinks(ConcurrentHashMap<String, Integer> crawledlist,
			ConcurrentHashMap<String, Integer> toCrawllist) {
		ConcurrentHashMap<String, Integer> crawledList = new ConcurrentHashMap<String, Integer>(crawledlist);
		ConcurrentHashMap<String, Integer> toCrawlList = new ConcurrentHashMap<String, Integer>(toCrawllist);
		ArrayList<Document> links = new ArrayList<Document>();
		Map.Entry<String, Integer> entry;
		Iterator<Map.Entry<String, Integer>> it = toCrawlList.entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			// System.out.println("url is " + entry.getKey());
			links.add(new Document("url", entry.getKey()).append("value", entry.getValue()));
		}
		Document doc = new Document("name", "toCrawlList").append("list", links);
		coll.replaceOne(eq("name", "toCrawlList"), doc);

		links = new ArrayList<>();
		it = crawledList.entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			links.add(new Document("url", entry.getKey()).append("value", entry.getValue()));
		}
		doc = new Document("name", "crawledList").append("list", links);
		coll.replaceOne(eq("name", "crawledList"), doc);
		System.out.println("links saved");
	}

	public void loadLinks(ConcurrentHashMap<String, Integer> crawledList,
			ConcurrentHashMap<String, Integer> toCrawlList) {

		MongoCursor<Document> cur = coll.find(eq("name", "crawledList")).iterator();
		while (cur.hasNext()) {
			ArrayList<Document> results = (ArrayList<Document>) cur.next().get("list");
			results.forEach(
					result -> crawledList.put(result.getString("url"), Integer.parseInt(result.getString("value"))));
		}

		cur = coll.find(eq("name", "crawledList")).iterator();
		while (cur.hasNext()) {
			ArrayList<Document> results = (ArrayList<Document>) cur.next().get("list");
			results.forEach(
					result -> toCrawlList.put(result.getString("url"), Integer.parseInt(result.getString("value"))));
		}

	}

	public void saveLexion(HashMap<String, Integer> lexion) {
		ArrayList<Document> words = new ArrayList<Document>();
		Iterator<Map.Entry<String, Integer>> it = lexion.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			words.add(new Document("word", entry.getKey()).append("df", entry.getValue()));
		}
		Document doc = new Document("name", "lexion").append("list", words);
		coll.replaceOne(eq("name", "lexion"), doc);
		System.out.println("lexion saved");
	}

	public HashMap<String, Integer> loadLexion() {
		MongoCursor<Document> cur = collection.find(eq("name", "lexion")).iterator();
		HashMap<String, Integer> lexion = new HashMap<String, Integer>();
		while (cur.hasNext()) {
			ArrayList<Document> results = (ArrayList<Document>) cur.next().get("list");
			results.forEach(result -> lexion.put(result.getString("word"), Integer.parseInt(result.getString("df"))));
		}
		return lexion;
	}

	public void addSuggestion(String suggestion) {
		coll.updateOne(eq("name","suggestions"),
				set("list.$",suggestion));
	}
	
	public ArrayList<String> findSuggestions(String st){
		MongoCursor<Document> cur = coll.find(combine(eq("name","suggestions"),regex("list.$","/^"+st+"/")))
				.iterator();
		ArrayList<String> array = new ArrayList<String>();
		while (cur.hasNext()) {
			array.add(cur.next().toString());
		}
		return array;
	}
}
