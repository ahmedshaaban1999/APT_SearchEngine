import com.mongodb.Block;
import com.mongodb.DBCursor;
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
	
	MongoClient mongoClient ;
	MongoDatabase database ;
	MongoDatabase db ;
	MongoCollection<Document> collection ;
	MongoCollection<Document> coll ;
	
	Block<Document> printBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	           System.out.println(document.toJson());
	       }
	};

	public boolean connect() {
		try {
		mongoClient = new MongoClient();
		database = mongoClient.getDatabase("test");
		db = mongoClient.getDatabase("info");
		//set timeout to 3 seconds
		database.withWriteConcern(new WriteConcern(3000));
		db.withWriteConcern(new WriteConcern(3000));
		collection = database.getCollection("col");
		coll = database.getCollection("coll");
		return true;
		}
		catch (Exception exp){
			return false;
		}
	}
	
	public boolean createDocument (SiteHits siteHits){
		try {
			ArrayList<Document> hits = new ArrayList<Document>();
			while(siteHits.hasHit()){
				Pair<Integer,String> tmp = siteHits.nextHit();
				hits.add(new Document("pos",tmp.getElement0())
						.append("type", tmp.getElement1()));
			}
		Document doc = new Document("word",siteHits.getWord())
				.append("stemmed", siteHits.getStemmed())
				.append("site hits",Arrays.asList(new Document("site",siteHits.getSite())
						.append("hits", hits)));
		collection.insertOne(doc);
		return true;
		} catch (Exception exp) {
			System.out.println("Error while creating document " + exp.getMessage());
			return false;
		}
	}
	
	public boolean updateDocument(SiteHits siteHits) {
		ArrayList<Document> hits = new ArrayList<Document>();
		while(siteHits.hasHit()){
			Pair<Integer,String> tmp = siteHits.nextHit();
			hits.add(new Document("pos",tmp.getElement0())
					.append("type", tmp.getElement1()));
		}
		Document doc = new Document("site","www.feedly.com")
				.append("hits", hits);
		
		collection.updateOne(combine(eq("word",siteHits.getWord()),eq("site hits.site",siteHits.getSite())),set("site hits.$",doc));
		
//		collection.updateOne(eq("word","forcing"),addToSet("site hits", doc));

//		collection.updateOne(
//                eq("_id", new ObjectId("57506d62f57802807471dd41")),
//                combine(set("stars", 1), set("contact.phone", "228-555-9999"), currentDate("lastModified")));
		return true;
	}
	
	public boolean deleteDocument(long id) {
		collection.deleteOne(eq("_id", new ObjectId("57506d62f57802807471dd41")));
		return true;
	}
	
	public void findDocument() {
		// a query written without helpers
		//collection.find(new Document("stars", new Document("$gte", 2)
		//	          	.append("$lt", 5))
		//	          	.append("categories", "Bakery")).forEach(printBlock);
		
		// a query written with helpers
		MongoCursor<Document> cur = collection.find(eq("site", "www.yahoo.com")).iterator();
		System.out.println(cur.next().getString("site"));
		
		collection.find(and(elemMatch("list")));
		
		// a projection written without helpers
		//collection.find(and(gte("stars", 2), lt("stars", 5), eq("categories", "Bakery")))
        //.projection(new Document("name", 1)
        //     .append("stars", 1)
        //     .append("categories",1)
        //     .append("_id", 0))
        //.forEach(printBlock);
		
		// a projection written with helpers
		//collection.find(and(gte("stars", 2), lt("stars", 5), eq("categories", "Bakery")))
        //.projection(fields(include("name", "stars", "categories"), excludeId()))
        //.forEach(printBlock);
		
	}
	
	public void saveLinks (ConcurrentHashMap<String, Integer> crawledList, ConcurrentHashMap<String, Integer> toCrawlList) {
		ArrayList<String> links = new ArrayList<>();
		String url;
		while(toCrawlList.keySet().iterator().hasNext()){
			url = (String) toCrawlList.keySet().iterator().next();
			links.add(url);
		}
		Document doc = new Document("name","crawledList").append("list",links);
		coll.insertOne(doc);
		
		links = new ArrayList<>();
		while(crawledList.keySet().iterator().hasNext()){
			url = (String) crawledList.keySet().iterator().next();
			links.add(url);
		}
		doc = new Document("name","toCrawlList").append("list",links);
		coll.insertOne(doc);
	}
	
	public void saveLexion (HashMap<String, Integer> lexion) {
		ArrayList<Document> words = new ArrayList<Document>();
		Iterator<Map.Entry<String, Integer>> it = lexion.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) it.next();
			words.add(new Document("word",entry.getKey())
					.append("tf", entry.getValue()));
		}
		Document doc = new Document("name","lexion")
				.append("list", words);
		coll.insertOne(doc);
	}
}
