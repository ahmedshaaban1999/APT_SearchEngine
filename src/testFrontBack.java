import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class testFrontBack {

	Database db;
	FrontBack fb;
	ConcurrentHashMap<String, Integer> crawledList = new ConcurrentHashMap<String, Integer>();
	HashMap<String, Integer> lexion = new HashMap<String, Integer>();
	
	public testFrontBack() {
		// TODO Auto-generated constructor stub
		db = new Database();
		fb = new FrontBack(crawledList,lexion);
	}

	@Test
	public void testProcessQuery() {
		//fail("Not yet implemented");
		String[] st = {"work"};
		fb.processQuery(st, "normal");
	}

}
